/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.GetMethod;
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener;
import com.owncloud.android.lib.common.network.WebdavUtils;
import com.owncloud.android.lib.common.operations.OperationCancelledException;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Remote operation performing the download of a remote file in the ownCloud server.
 *
 * @author David A. Velasco
 * @author masensio
 */

public class DownloadFileRemoteOperation extends RemoteOperation {

    private static final String TAG = DownloadFileRemoteOperation.class.getSimpleName();

    private final Set<OnDatatransferProgressListener> mDataTransferListeners = new HashSet<>();
    private final AtomicBoolean mCancellationRequested = new AtomicBoolean(false);
    private long modificationTimestamp = 0;
    private String eTag = "";
    private GetMethod getMethod;

    private final String remotePath;
    private final String temporalFolderPath;

    /**
     * @param remotePath         which file to download
     * @param temporalFolderPath temporal folder where file is stored, to avoid conflicts it use full remote path
     */
    public DownloadFileRemoteOperation(String remotePath, String temporalFolderPath) {
        this.remotePath = remotePath;
        this.temporalFolderPath = temporalFolderPath;
    }

	@Override
	public RemoteOperationResult run(NextcloudClient client) {
        RemoteOperationResult<?> result;
        File tmpFile = new File(getTmpPath());

        try {
            final var parentFile = tmpFile.getParentFile();
            if (parentFile == null) {
                return new RemoteOperationResult<>(new Exception("parent file is null"));
            }

            boolean isTempFileCreated = parentFile.mkdirs();
            if (isTempFileCreated) {
                Log_OC.d(TAG, "temp file created");
            }

            int status = downloadFile(client, tmpFile);
            result = new RemoteOperationResult<>(isSuccess(status), getMethod);
            Log_OC.i(TAG, "Download of " + remotePath + " to " + getTmpPath() + ": " +
                result.getLogMessage());

        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Download of " + remotePath + " to " + getTmpPath() + ": " +
                result.getLogMessage(), e);
        }

        return result;
    }


    private int downloadFile(NextcloudClient client, File targetFile) throws IOException, OperationCancelledException, CreateLocalFileException {
        int status;
        getMethod = new GetMethod(client.getFilesDavUri(remotePath), false);
        Iterator<OnDatatransferProgressListener> it;

        FileOutputStream fos = null;
        try {
            status = client.execute(getMethod);

            if (isSuccess(status)) {
                try {
                    boolean isTargetFileCreated = targetFile.createNewFile();
                    if (isTargetFileCreated) {
                        Log_OC.i(TAG, "target file is created");
                    }
                } catch (IOException | SecurityException ex) {
                    Log_OC.e(TAG, "Error creating file " + targetFile.getAbsolutePath(), ex);
                    throw new CreateLocalFileException(targetFile.getPath(), ex);
                }

                BufferedInputStream bis = new BufferedInputStream(getMethod.getResponseBodyAsStream());
                fos = new FileOutputStream(targetFile);

                byte[] bytes = new byte[4096];
                int readResult;
                while ((readResult = bis.read(bytes)) != -1) {
                    synchronized (mCancellationRequested) {
                        if (mCancellationRequested.get()) {
                            throw new OperationCancelledException();
                        }
                    }

                    fos.write(bytes, 0, readResult);

                    synchronized (mDataTransferListeners) {
                        it = mDataTransferListeners.iterator();
                    }
                }


                String modificationTime = getMethod.getResponseHeader("Last-Modified");
                if (modificationTime == null) {
                    modificationTime = getMethod.getResponseHeader("last-modified");
                }

                if (modificationTime != null) {
                    Date d = WebdavUtils.parseResponseDate(modificationTime);
                    modificationTimestamp = (d != null) ? d.getTime() : 0;
                } else {
                    Log_OC.e(TAG, "Could not read modification time from response downloading " + remotePath);
                }

                eTag = WebdavUtils.getEtagFromResponse(getMethod);
                if (eTag.isEmpty()) {
                    Log_OC.e(TAG, "Could not read eTag from response downloading " + remotePath);
                }
            }
        } finally {
            if (fos != null) fos.close();
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
