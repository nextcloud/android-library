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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class ChunkedFileUploadRemoteOperation extends UploadFileRemoteOperation {

    public static final long CHUNK_SIZE_MOBILE = 1024000;
    public static final long CHUNK_SIZE_WIFI = 10240000;
    private static final String OC_CHUNK_X_OC_MTIME_HEADER = "X-OC-Mtime";
    private static final String TAG = ChunkedFileUploadRemoteOperation.class.getSimpleName();
    private final boolean onWifiConnection;

    public ChunkedFileUploadRemoteOperation(String storagePath,
                                            String remotePath,
                                            String mimeType,
                                            String requiredEtag,
                                            String lastModificationTimestamp,
                                            boolean onWifiConnection) {
        this(storagePath, remotePath, mimeType, requiredEtag, lastModificationTimestamp, onWifiConnection, null);
    }

    public ChunkedFileUploadRemoteOperation(String storagePath,
                                            String remotePath,
                                            String mimeType,
                                            String requiredEtag,
                                            String lastModificationTimestamp,
                                            boolean onWifiConnection,
                                            boolean disableRetries) {
        this(storagePath, remotePath, mimeType, requiredEtag, lastModificationTimestamp, onWifiConnection, null, disableRetries);
    }

    public ChunkedFileUploadRemoteOperation(String storagePath,
                                            String remotePath,
                                            String mimeType,
                                            String requiredEtag,
                                            String lastModificationTimestamp,
                                            boolean onWifiConnection,
                                            String token) {
        this(storagePath, remotePath, mimeType, requiredEtag, lastModificationTimestamp, onWifiConnection, token, true);
    }

    public ChunkedFileUploadRemoteOperation(String storagePath,
                                            String remotePath,
                                            String mimeType,
                                            String requiredEtag,
                                            String lastModificationTimestamp,
                                            boolean onWifiConnection,
                                            String token,
                                            boolean disableRetries) {
        super(storagePath, remotePath, mimeType, requiredEtag, lastModificationTimestamp, token, disableRetries);
        this.onWifiConnection = onWifiConnection;
    }

    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result;
        DefaultHttpMethodRetryHandler oldRetryHandler = (DefaultHttpMethodRetryHandler) 
                client.getParams().getParameter(HttpMethodParams.RETRY_HANDLER);
        File file = new File(localPath);

        try {
            if (disableRetries) {
                // prevent that uploads are retried automatically by network library
                client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                                                new DefaultHttpMethodRetryHandler(0, false));
            }

            String uploadFolderUri = client.getUploadUri() + "/" + client.getUserId() + "/" + FileUtils.md5Sum(file);

            // create folder
            MkColMethod createFolder = new MkColMethod(uploadFolderUri);

            client.executeMethod(createFolder, 30000, 5000);
            
            // list chunks
            PropFindMethod listChunks = new PropFindMethod(uploadFolderUri,
                    WebdavUtils.getChunksPropSet(),
                                                           DavConstants.DEPTH_1);

            client.executeMethod(listChunks);
            
            if (!listChunks.succeeded()) {
                return new RemoteOperationResult(listChunks.succeeded(), listChunks);
            }
            
            List<Chunk> chunksOnServer = new ArrayList<>();

            MultiStatus dataInServer = listChunks.getResponseBodyAsMultiStatus();

            WebdavEntry we;
            for (MultiStatusResponse response : dataInServer.getResponses()) {
                we = new WebdavEntry(response, client.getUploadUri().getPath());

                if (!".file".equalsIgnoreCase(we.getName()) && !we.isDirectory()) {
                    String[] part = we.getName().split("-");
                    chunksOnServer.add(new Chunk(Long.parseLong(part[0]), Long.parseLong(part[1])));
                }
            }

            // chunk length
            long chunkSize;
            if (onWifiConnection) {
                chunkSize = CHUNK_SIZE_WIFI;
            } else {
                chunkSize = CHUNK_SIZE_MOBILE;
            }

            // check for missing chunks
            List<Chunk> missingChunks = checkMissingChunks(chunksOnServer, file.length(), chunkSize);

            // upload chunks
            for (Chunk missingChunk : missingChunks) {
                RemoteOperationResult chunkResult = uploadChunk(client, uploadFolderUri, missingChunk);
                
                if (!chunkResult.isSuccess()) {
                    return chunkResult;
                }

                if (cancellationRequested.get()) {
                    return new RemoteOperationResult(new OperationCancelledException());
                }
            }

            // assemble
            String destinationUri = client.getNewWebdavUri() + "/files/" + client.getUserId() +
                    WebdavUtils.encodePath(remotePath);
            String originUri = uploadFolderUri + "/.file";
            MoveMethod moveMethod = new MoveMethod(originUri, destinationUri, true);
            moveMethod.addRequestHeader(OC_CHUNK_X_OC_MTIME_HEADER, String.valueOf(file.lastModified() / 1000));

            if (token != null) {
                moveMethod.addRequestHeader(E2E_TOKEN, token);
            }
            int moveResult = client.executeMethod(moveMethod);

            result = new RemoteOperationResult(isSuccess(moveResult), moveMethod);
        } catch (Exception e) {
            if (putMethod != null && putMethod.isAborted()) {
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

    List<Chunk> checkMissingChunks(List<Chunk> chunks, long length, long chunkSize) {
        List<Chunk> missingChunks = new ArrayList<>();

        long start = 0;

        while (start <= length) {
            Chunk nextChunk = findNextFittingChunk(chunks, start, chunkSize);

            if (nextChunk == null) {
                // create new chunk
                long end;

                if (start + chunkSize <= length) {
                    end = start + chunkSize - 1;
                } else {
                    end = length;
                }

                missingChunks.add(new Chunk(start, end));
                start = end + 1;
            } else if (nextChunk.start == start) {
                // go to next
                start = start + nextChunk.length();
            } else {
                // fill the gap
                missingChunks.add(new Chunk(start, nextChunk.start - 1));
                start = nextChunk.start;
            }
        }

        return missingChunks;
    }

    private Chunk findNextFittingChunk(List<Chunk> chunks, long start, long length) {
        for (Chunk chunk : chunks) {
            if (chunk.start >= start && (chunk.start - start) <= length) {
                return chunk;
            }
        }
        return null;
    }

    private RemoteOperationResult uploadChunk(OwnCloudClient client, String uploadFolderUri, Chunk chunk)
            throws IOException {
        int status;
        RemoteOperationResult result;

        String startString = String.format(Locale.ROOT, "%016d", chunk.start);
        String endString = String.format(Locale.ROOT, "%016d", chunk.end);

        FileChannel channel = null;
        RandomAccessFile raf = null;

        File file = new File(localPath);

        try {
            raf = new RandomAccessFile(file, "r");
            channel = raf.getChannel();
            entity = new ChunkFromFileChannelRequestEntity(channel, mimeType, chunk.start, chunk.length(), file);
            
            synchronized (dataTransferListeners) {
                ((ProgressiveDataTransfer) entity).addDataTransferProgressListeners(dataTransferListeners);
            }

            String chunkUri = uploadFolderUri + "/" + startString + "-" + endString;

            if (putMethod != null) {
                putMethod.releaseConnection(); // let the connection available for other methods
            }

            putMethod = createPutMethod(chunkUri);

            if (token != null) {
                putMethod.addRequestHeader(E2E_TOKEN, token);
            }

            status = client.executeMethod(putMethod);

            result = new RemoteOperationResult(isSuccess(status), putMethod);

            client.exhaustResponse(putMethod.getResponseBodyAsStream());
            Log_OC.d(TAG, "Upload of " + localPath + " to " + remotePath + ", chunk from " + startString + " to " +
                    endString + " size: "  + chunk.length() + ", HTTP result status " + status);
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
}
