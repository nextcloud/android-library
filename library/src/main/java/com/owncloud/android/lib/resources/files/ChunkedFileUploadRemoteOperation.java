/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files;

import android.text.TextUtils;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory;
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

import java.security.MessageDigest;
import java.nio.ByteBuffer;
import java.math.BigInteger;

public class ChunkedFileUploadRemoteOperation extends UploadFileRemoteOperation {

    public static final long CHUNK_SIZE_MOBILE = 10240000;
    public static final long CHUNK_SIZE_WIFI = 40960000;
    public static final String DESTINATION_HEADER = "Destination";
    public static final int CHUNK_NAME_LENGTH = 6;
    private static final String TAG = ChunkedFileUploadRemoteOperation.class.getSimpleName();
    public final int ASSEMBLE_TIME_MIN = 30 * 1000; // 30s
    public final int ASSEMBLE_TIME_MAX = 30 * 60 * 1000; // 30min
    public final int ASSEMBLE_TIME_PER_GB = 3 * 60 * 1000; // 3 min
    private final boolean onWifiConnection;
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

    protected static Chunk calcNextChunk(long fileSize, int chunkId, long startByte, long chunkSize) {
        if (chunkId < 0 || String.valueOf(chunkId).length() > CHUNK_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "chunkId must not exceed length specified in CHUNK_NAME_LENGTH (" + CHUNK_NAME_LENGTH + ")");
        }

        long length = startByte + chunkSize > fileSize ? fileSize - startByte : chunkSize;
        return new Chunk(chunkId, startByte, length);
    }

    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result;
        DefaultHttpMethodRetryHandler oldRetryHandler = (DefaultHttpMethodRetryHandler) client.getParams()
                .getParameter(HttpMethodParams.RETRY_HANDLER);
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
            PropFindMethod listChunks = new PropFindMethod(uploadFolderUri,
                                                           WebdavUtils.getChunksPropSet(),
                                                           DavConstants.DEPTH_1);

            client.executeMethod(listChunks);

            if (!listChunks.succeeded()) {
                return new RemoteOperationResult(listChunks.succeeded(), listChunks);
            }

            MultiStatus dataInServer = listChunks.getResponseBodyAsMultiStatus();

            // determine chunks already on server
            // chunks are assumed to be uploaded linearly, starting at 0B
            long nextByte = 0;
            int lastId = 0;
            for (MultiStatusResponse response : dataInServer.getResponses()) {
                WebdavEntry we = new WebdavEntry(response, Objects.requireNonNull(client.getUploadUri().getPath()));
                String name = we.getName();

                // filter out any objects not matching expected chunk name
                if (!we.isDirectory() && name != null && (name.length() <= CHUNK_NAME_LENGTH) &&
                        TextUtils.isDigitsOnly(name)) {
                    // is part of upload
                    int id = Integer.parseInt(name);
                    if (id > lastId) {
                        lastId = id;
                    }
                    nextByte += we.getContentLength();
                }
            }

            // iteratively upload remaining chunks
            while (nextByte + 1 < file.length()) {
                // determine size of next chunk
                Chunk chunk = calcNextChunk(file.length(), ++lastId, nextByte, chunkSize);

                RemoteOperationResult chunkResult = uploadChunk(client, chunk);
                if (!chunkResult.isSuccess()) {
                    return chunkResult;
                }

                if (cancellationRequested.get()) {
                    return new RemoteOperationResult(new OperationCancelledException());
                }

                nextByte += chunk.getLength();
            }

            // assemble
            String originUri = uploadFolderUri + "/.file";

            moveMethod = new MoveMethod(originUri, destinationUri, true);
            moveMethod.addRequestHeader(OC_X_OC_MTIME_HEADER, String.valueOf(lastModificationTimestamp));

            File localFile = new File(localPath);
            String hash = FileUtils.getHashFromFile(this, localFile, "SHA-256");
            if(hash != null) {
                putMethod.addRequestHeader("X-Content-Hash", hash);
            }

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
            entity = new ChunkFromFileChannelRequestEntity(channel,
                                                           mimeType,
                                                           chunk.getStart(),
                                                           chunk.getLength(),
                                                           file);

            synchronized (dataTransferListeners) {
                ((ProgressiveDataTransfer) entity).addDataTransferProgressListeners(dataTransferListeners);
            }

            // pad chunk name to 6 digits
            String chunkUri =
                    uploadFolderUri + "/" + String.format(Locale.ROOT, "%0" + CHUNK_NAME_LENGTH + "d", chunk.getId());

            if (putMethod != null) {
                putMethod.releaseConnection(); // let the connection available for other methods
            }

            putMethod = createPutMethod(chunkUri);

            putMethod.addRequestHeader(DESTINATION_HEADER, destinationUri);

            if (token != null) {
                putMethod.addRequestHeader(E2E_TOKEN, token);
            }

            if (OwnCloudClientManagerFactory.getHashCheck()) {
                try (RandomAccessFile hashRaf = new RandomAccessFile(file, "r")) {
                    MessageDigest md = MessageDigest.getInstance("SHA-256");

                    FileChannel hashChannel = hashRaf.getChannel();
                    ByteBuffer buf = ByteBuffer.allocate((int) chunk.getLength());
                    hashChannel.position(chunk.getStart());
                    hashChannel.read(buf);
                    md.update(buf.array());

                    String chunkHash = String.format("%064x", new BigInteger(1, md.digest()));

                    putMethod.addRequestHeader("X-Content-Hash", chunkHash);
                } catch (Exception e) {
                    Log_OC.w(TAG, "Could not compute chunk hash");
                }
            }

            status = client.executeMethod(putMethod);

            result = new RemoteOperationResult(isSuccess(status), putMethod);

            client.exhaustResponse(putMethod.getResponseBodyAsStream());
            Log_OC.d(TAG,
                     "Upload of " + localPath + " to " + remotePath + ", chunk id: " + chunk.getId() + " from " +
                             chunk.getStart() + " size: " + chunk.getLength() + ", HTTP result status " + status);
        } finally {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e) {
                    Log_OC.e(TAG, "Error closing file channel!", e);
                }
            }
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    Log_OC.e(TAG, "Error closing file access!", e);
                }
            }
            if (putMethod != null) {
                putMethod.releaseConnection(); // let the connection available for other methods
            }
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
