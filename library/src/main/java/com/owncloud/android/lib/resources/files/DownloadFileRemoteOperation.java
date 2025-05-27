/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory;
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener;
import com.owncloud.android.lib.common.network.WebdavUtils;
import com.owncloud.android.lib.common.operations.OperationCancelledException;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import java.util.Locale;

/**
 * Remote operation performing the download of a remote file in the ownCloud server.
 *
 * @author David A. Velasco
 * @author masensio
 */

public class DownloadFileRemoteOperation extends RemoteOperation {

    private static final String TAG = DownloadFileRemoteOperation.class.getSimpleName();

    private Set<OnDatatransferProgressListener> mDataTransferListeners = new HashSet<>();
    private final AtomicBoolean mCancellationRequested = new AtomicBoolean(false);
    private long modificationTimestamp = 0;
    private String eTag = "";
    private GetMethod getMethod;

    private String remotePath;
    private String temporalFolderPath;

    /**
     * @param remotePath         which file to download
     * @param temporalFolderPath temporal folder where file is stored, to avoid conflicts it use full remote path
     */
    public DownloadFileRemoteOperation(String remotePath, String temporalFolderPath) {
        this.remotePath = remotePath;
        this.temporalFolderPath = temporalFolderPath;
    }

	@Override
	protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result;

        /// download will be performed to a temporal file, then moved to the final location
        File tmpFile = new File(getTmpPath());

        /// perform the download
        try {
            tmpFile.getParentFile().mkdirs();
            int status = downloadFile(client, tmpFile);
            result = new RemoteOperationResult(isSuccess(status), getMethod);
            Log_OC.i(TAG, "Download of " + remotePath + " to " + getTmpPath() + ": " +
                result.getLogMessage());

        } catch (Exception e) {
            result = new RemoteOperationResult(e);
            Log_OC.e(TAG, "Download of " + remotePath + " to " + getTmpPath() + ": " +
                result.getLogMessage(), e);
        }

        return result;
    }


    private int downloadFile(OwnCloudClient client, File targetFile) throws IOException, OperationCancelledException, CreateLocalFileException {
        int status;
        boolean savedFile = false;
        getMethod = new GetMethod(client.getFilesDavUri(remotePath));
        Iterator<OnDatatransferProgressListener> it;

        FileOutputStream fos = null;
        try {
            status = client.executeMethod(getMethod);
            if (isSuccess(status)) {
                try {
                    targetFile.createNewFile();
                } catch (IOException | SecurityException ex) {
                    Log_OC.e(TAG, "Error creating file " + targetFile.getAbsolutePath(), ex);
                    throw new CreateLocalFileException(targetFile.getPath(), ex);
                }
                BufferedInputStream bis = new BufferedInputStream(getMethod.getResponseBodyAsStream());
                fos = new FileOutputStream(targetFile);
                long transferred = 0;

                Header contentLength = getMethod.getResponseHeader("Content-Length");
                long totalToTransfer = (contentLength != null &&
                        contentLength.getValue().length() > 0) ?
                        Long.parseLong(contentLength.getValue()) : 0;

                byte[] bytes = new byte[4096];
                int readResult;
                while ((readResult = bis.read(bytes)) != -1) {
                    synchronized (mCancellationRequested) {
                        if (mCancellationRequested.get()) {
                            getMethod.abort();
                            throw new OperationCancelledException();
                        }
                    }
                    fos.write(bytes, 0, readResult);
                    transferred += readResult;
                    synchronized (mDataTransferListeners) {
                        it = mDataTransferListeners.iterator();
                        while (it.hasNext()) {
                            it.next().onTransferProgress(readResult, transferred, totalToTransfer,
                                targetFile.getName());
                        }
                    }
                }
                // Check if the file is completed
                // if transfer-encoding: chunked we cannot check if the file is complete
                Header transferEncodingHeader = getMethod.getResponseHeader("Transfer-Encoding");
                boolean transferEncoding = false;

                if (transferEncodingHeader != null) {
                    transferEncoding = "chunked".equals(transferEncodingHeader.getValue());
                }
                
                if (transferred == totalToTransfer || transferEncoding) {
                    if (OwnCloudClientManagerFactory.getHASHcheck()){
                        Header hashHeader = getMethod.getResponseHeader("X-Content-Hash");
                        String expectedHash = hashHeader != null ? hashHeader.getValue() : null;
                        if (expectedHash != null) {
                            try {
                                String[] entries = expectedHash.split(",");
                                for (String entry : entries) {
                                    String[] parts = entry.split(";", 2);
                                    if (parts.length != 2) continue;
                                    String Algorithm  = parts[0].trim().toLowerCase(Locale.ROOT);
                                    String hash  = parts[1].trim();

                                    String digestAlgorithm = null;

                                    switch (Algorithm) {
                                        case "sha256":

                                            digestAlgorithm = "SHA-256";
                                            break;
                                        default:
                                            // Skip unknown algorithm
                                            continue;
                                    }

                                    String fileHash = FileUtils.getHashFromFile(this, targetFile, digestAlgorithm);

                                    if (!hash.equalsIgnoreCase(fileHash)) {
                                        // Hash is incorrect: delete file and abort
                                        Log_OC.w(TAG, "Hash mismatch: expected="+ hash +" actual="+ FileHash);
                                        status = 418;
                                        savedFile = false;
                                    }else{
                                        savedFile = true;
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                Log_OC.w(TAG, "Could not compute chunk hash");
                                status = 418;
                                savedFile = false;
                            }
                        }else {
                            savedFile = true;
                        }
                    }else {
                        savedFile = true;
                    }

                    Header modificationTime = getMethod.getResponseHeader("Last-Modified");
                    if (modificationTime == null) {
                        modificationTime = getMethod.getResponseHeader("last-modified");
                    }
                    if (modificationTime != null) {
                        Date d = WebdavUtils.parseResponseDate(modificationTime.getValue());
                        modificationTimestamp = (d != null) ? d.getTime() : 0;
                    } else {
                        Log_OC.e(TAG, "Could not read modification time from response downloading " + remotePath);
                    }

                    eTag = WebdavUtils.getEtagFromResponse(getMethod);
                    if (eTag.length() == 0) {
                        Log_OC.e(TAG, "Could not read eTag from response downloading " + remotePath);
                    }

                } else {
                    client.exhaustResponse(getMethod.getResponseBodyAsStream());
                    // TODO some kind of error control!
                }

            } else {
                client.exhaustResponse(getMethod.getResponseBodyAsStream());
            }

        } finally {
            if (fos != null) fos.close();
            if (!savedFile && targetFile.exists()) {
                targetFile.delete();
            }
            getMethod.releaseConnection();    // let the connection available for other methods
        }
        return status;
    }

    private boolean isSuccess(int status) {
        return (status == HttpStatus.SC_OK);
    }

    private String getTmpPath() {
        return temporalFolderPath + remotePath;
    }

    public void addDatatransferProgressListener(OnDatatransferProgressListener listener) {
        synchronized (mDataTransferListeners) {
            mDataTransferListeners.add(listener);
        }
    }

    public void removeDatatransferProgressListener(OnDatatransferProgressListener listener) {
        synchronized (mDataTransferListeners) {
            mDataTransferListeners.remove(listener);
        }
    }

    public void cancel() {
        mCancellationRequested.set(true);   // atomic set; there is no need of synchronizing it
    }

    public long getModificationTimestamp() {
        return modificationTimestamp;
    }

    public String getEtag() {
        return eTag;
    }
}
