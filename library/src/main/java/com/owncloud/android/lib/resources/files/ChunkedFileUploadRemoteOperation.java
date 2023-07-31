/* ownCloud Android Library is available under MIT license
 *   Copyright (C) 2015 ownCloud Inc.
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 */

package com.owncloud.android.lib.resources.files;

import android.text.TextUtils;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.network.ChunkFromFileChannelRequestEntity;
import com.owncloud.android.lib.common.network.ProgressiveDataTransfer;
import com.owncloud.android.lib.common.network.WebdavEntry;
import com.owncloud.android.lib.common.network.WebdavUtils;
import com.owncloud.android.lib.common.operations.OperationCancelledException;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.MkColMethod;
import org.apache.jackrabbit.webdav.client.methods.MoveMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.VisibleForTesting;


public class ChunkedFileUploadRemoteOperation extends UploadFileRemoteOperation {

    public static final long CHUNK_SIZE_MOBILE = 1024000;
    public static final long CHUNK_SIZE_WIFI = 10240000;
    public static final String DESTINATION_HEADER = "Destination";
    private static final String TAG = ChunkedFileUploadRemoteOperation.class.getSimpleName();
    private final boolean onWifiConnection;

    public final int ASSEMBLE_TIME_MIN = 30 * 1000; // 30s
    public final int ASSEMBLE_TIME_MAX = 30 * 60 * 1000; // 30min
    public final int ASSEMBLE_TIME_PER_GB = 3 * 60 * 1000; // 3 min

    private String uploadFolderUri;
    private String destinationUri;

    public ChunkedFileUploadRemoteOperation(String storagePath,
                                            String remotePath,
                                            String mimeType,
                                            String requiredEtag,
                                            long lastModificationTimestamp,
                                            boolean onWifiConnection) {
        this(storagePath, remotePath, mimeType, requiredEtag, lastModificationTimestamp, onWifiConnection, null);
    }

    public ChunkedFileUploadRemoteOperation(String storagePath,
                                            String remotePath,
                                            String mimeType,
                                            String requiredEtag,
                                            long lastModificationTimestamp,
                                            Long creationTimestamp,
                                            boolean onWifiConnection,
                                            boolean disableRetries) {
        this(storagePath,
                remotePath,
                mimeType,
                requiredEtag,
                lastModificationTimestamp,
                onWifiConnection,
                null,
                creationTimestamp,
                disableRetries);
    }

    public ChunkedFileUploadRemoteOperation(String storagePath,
                                            String remotePath,
                                            String mimeType,
                                            String requiredEtag,
                                            long lastModificationTimestamp,
                                            boolean onWifiConnection,
                                            String token) {
        this(storagePath,
                remotePath,
                mimeType,
                requiredEtag,
                lastModificationTimestamp,
                onWifiConnection,
                token,
                null,
                true);
    }

    public ChunkedFileUploadRemoteOperation(String storagePath,
                                            String remotePath,
                                            String mimeType,
                                            String requiredEtag,
                                            long lastModificationTimestamp,
                                            boolean onWifiConnection,
                                            String token,
                                            Long creationTimestamp,
                                            boolean disableRetries) {
        super(storagePath,
                remotePath,
                mimeType,
                requiredEtag,
                lastModificationTimestamp,
                creationTimestamp,
                token,
                disableRetries);
        this.onWifiConnection = onWifiConnection;
    }

    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result;
        DefaultHttpMethodRetryHandler oldRetryHandler = (DefaultHttpMethodRetryHandler) 
                client.getParams().getParameter(HttpMethodParams.RETRY_HANDLER);
        File file = new File(localPath);
        MoveMethod moveMethod = null;
        try {
            if (disableRetries) {
                // prevent that uploads are retried automatically by network library
                client.getParams()
                        .setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
            }

            // chunk length
            long chunkSize;
            if (onWifiConnection) {
                chunkSize = CHUNK_SIZE_WIFI;
            } else {
                chunkSize = CHUNK_SIZE_MOBILE;
            }

            uploadFolderUri = client.getUploadUri() + "/" + client.getUserId() + "/" + FileUtils.md5Sum(file);

            destinationUri = client.getDavUri() + "/files/" + client.getUserId() + WebdavUtils.encodePath(remotePath);

            // create folder
            MkColMethod createFolder = new MkColMethod(uploadFolderUri);

            createFolder.addRequestHeader(DESTINATION_HEADER, destinationUri);

            client.executeMethod(createFolder, 30000, 5000);

            // list chunks
            PropFindMethod listChunks =
                    new PropFindMethod(uploadFolderUri, WebdavUtils.getChunksPropSet(), DavConstants.DEPTH_1);

            client.executeMethod(listChunks);

            if (!listChunks.succeeded()) {
                return new RemoteOperationResult(listChunks.succeeded(), listChunks);
            }

            MultiStatus dataInServer = listChunks.getResponseBodyAsMultiStatus();

            // determine chunks already on server
            // chunks are assumed to be uploaded linearly, starting at 0B
            WebdavEntry we;
            long lastByte = 0;
            int lastId = 0;
            for (MultiStatusResponse response : dataInServer.getResponses()) {
                we = new WebdavEntry(response, Objects.requireNonNull(client.getUploadUri().getPath()));

                if (!we.isDirectory() && (Objects.requireNonNull(we.getName()).length() == 6) &&
                        TextUtils.isDigitsOnly(we.getName())) {
                    int id = Integer.parseInt(we.getName());
                    if (id > lastId) {
                        lastId = id;
                    }
                    lastByte += we.getContentLength();
                }
            }

            while (lastByte < file.length()) {
                long length = lastByte + chunkSize > file.length() - 1 ? file.length() - lastByte - 1 : chunkSize;
                Chunk chunk = new Chunk(++lastId, lastByte + 1, length);

                RemoteOperationResult chunkResult = uploadChunk(client, chunk);
                if (!chunkResult.isSuccess()) {
                    return chunkResult;
                }

                if (cancellationRequested.get()) {
                    return new RemoteOperationResult(new OperationCancelledException());
                }

                lastByte += chunkSize;
            }

            // assemble
            String originUri = uploadFolderUri + "/.file";

            moveMethod = new MoveMethod(originUri, destinationUri, true);
            moveMethod.addRequestHeader(
                    OC_X_OC_MTIME_HEADER,
                    String.valueOf(lastModificationTimestamp)
            );

            if (creationTimestamp != null && creationTimestamp > 0) {
                moveMethod.addRequestHeader(OC_X_OC_CTIME_HEADER, String.valueOf(creationTimestamp));
            }

            if (token != null) {
                moveMethod.addRequestHeader(E2E_TOKEN, token);
            }

            final int DO_NOT_CHANGE_DEFAULT = -1;
            int moveResult = client.executeMethod(moveMethod, calculateAssembleTimeout(file), DO_NOT_CHANGE_DEFAULT);

            result = new RemoteOperationResult(isSuccess(moveResult), moveMethod);
        } catch (Exception e) {
            if (putMethod != null && putMethod.isAborted()) {
                if (cancellationRequested.get() && cancellationReason != null) {
                    result = new RemoteOperationResult(cancellationReason);
                } else {
                    result = new RemoteOperationResult(new OperationCancelledException());
                }
            } else if (moveMethod != null && moveMethod.isAborted()) {
                if (cancellationRequested.get() && cancellationReason != null) {
                    result = new RemoteOperationResult(cancellationReason);
                } else {
                    result = new RemoteOperationResult(new OperationCancelledException());
                }
            } else {
                result = new RemoteOperationResult(e);
            }
        } finally {
            if (disableRetries) {
                // reset previous retry handler
                client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, oldRetryHandler);
            }
        }
        return result;
    }

    private RemoteOperationResult uploadChunk(OwnCloudClient client, Chunk chunk) throws IOException {
        int status;
        RemoteOperationResult result;

        FileChannel channel = null;
        RandomAccessFile raf = null;

        File file = new File(localPath);

        try {
            raf = new RandomAccessFile(file, "r");
            channel = raf.getChannel();
            entity = new ChunkFromFileChannelRequestEntity(channel, mimeType, chunk.start, chunk.length, file);
            
            synchronized (dataTransferListeners) {
                ((ProgressiveDataTransfer) entity).addDataTransferProgressListeners(dataTransferListeners);
            }

            String chunkUri = uploadFolderUri + "/" + String.format(Locale.ROOT, "%06d", chunk.id);

            if (putMethod != null) {
                putMethod.releaseConnection(); // let the connection available for other methods
            }

            putMethod = createPutMethod(chunkUri);

            putMethod.addRequestHeader(DESTINATION_HEADER, destinationUri);

            if (token != null) {
                putMethod.addRequestHeader(E2E_TOKEN, token);
            }

            status = client.executeMethod(putMethod);

            result = new RemoteOperationResult(isSuccess(status), putMethod);

            client.exhaustResponse(putMethod.getResponseBodyAsStream());
            Log_OC.d(TAG,
                     "Upload of " + localPath + " to " + remotePath + ", chunk id: " + chunk.id + " from " +
                             chunk.start + " size: " + chunk.length + ", HTTP result status " + status);
        } finally {
            if (channel != null)
                try {
                    channel.close();
                } catch (IOException e) {
                    Log_OC.e(TAG, "Error closing file channel!", e);
                }
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    Log_OC.e(TAG, "Error closing file access!", e);
                }
            }
            if (putMethod != null)
                putMethod.releaseConnection(); // let the connection available for other methods
        }
        return result;
    }

    private PutMethod createPutMethod(String uriPrefix) {
        putMethod = new PutMethod(uriPrefix);
        putMethod.setRequestEntity(entity);
        if (cancellationRequested.get()) {
            putMethod.abort(); // next method will throw an exception
        }

        return putMethod;
    }

    @VisibleForTesting
    public int calculateAssembleTimeout(File file) {
        final double fileSizeInGb = file.length() / 1e9;

        return Math.max(ASSEMBLE_TIME_MIN, Math.min((int) (ASSEMBLE_TIME_PER_GB * fileSizeInGb), ASSEMBLE_TIME_MAX));
    }
}
