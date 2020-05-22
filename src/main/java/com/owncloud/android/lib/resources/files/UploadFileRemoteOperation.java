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
import com.owncloud.android.lib.common.network.FileRequestEntity;
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener;
import com.owncloud.android.lib.common.network.ProgressiveDataTransfer;
import com.owncloud.android.lib.common.network.WebdavUtils;
import com.owncloud.android.lib.common.operations.OperationCancelledException;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Remote operation performing the upload of a remote file to the ownCloud server.
 * 
 * @author David A. Velasco
 * @author masensio
 */

public class UploadFileRemoteOperation extends RemoteOperation {
	private static final String OC_TOTAL_LENGTH_HEADER = "OC-Total-Length";
	private static final String IF_MATCH_HEADER = "If-Match";
    private static final String OC_X_OC_MTIME_HEADER = "X-OC-Mtime";

	protected String localPath;
	protected String remotePath;
	protected String mimeType;
	private String lastModificationTimestamp;
	PutMethod putMethod = null;
	private String requiredEtag = null;
    String token = null;

	final AtomicBoolean cancellationRequested = new AtomicBoolean(false);
    RemoteOperationResult.ResultCode cancellationReason = null;
	final Set<OnDatatransferProgressListener> dataTransferListeners = new HashSet<>();

	protected RequestEntity entity = null;

    public UploadFileRemoteOperation(String localPath,
                                     String remotePath,
                                     String mimeType,
                                     String requiredEtag,
                                     String lastModificationTimestamp) {
        this(localPath, remotePath, mimeType, requiredEtag, lastModificationTimestamp, null);
    }

    public UploadFileRemoteOperation(String localPath,
                                     String remotePath,
                                     String mimeType,
									 String lastModificationTimestamp) {
		this.localPath = localPath;
		this.remotePath = remotePath;
		this.mimeType = mimeType;

        if (lastModificationTimestamp == null) {
            throw new AssertionError("LastModificationTimestamp may NOT be null!");
        }

        // TODO check for max value of lastModificationTimestamp

		this.lastModificationTimestamp = lastModificationTimestamp;
	}

    public UploadFileRemoteOperation(String localPath,
                                     String remotePath,
                                     String mimeType,
                                     String requiredEtag,
                                     String lastModificationTimestamp,
                                     String token) {
		this(localPath, remotePath, mimeType, lastModificationTimestamp);
		this.requiredEtag = requiredEtag;
        this.token = token;
	}

	@Override
	protected RemoteOperationResult run(OwnCloudClient client) {
		RemoteOperationResult result;
		DefaultHttpMethodRetryHandler oldRetryHandler =
			(DefaultHttpMethodRetryHandler) client.getParams().getParameter(HttpMethodParams.RETRY_HANDLER);

		try {
			// prevent that uploads are retried automatically by network library
			client.getParams().setParameter(
				HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler(0, false)
			);

			putMethod = new PutMethod(client.getWebdavUri() + WebdavUtils.encodePath(remotePath));

            if (token != null) {
                putMethod.addRequestHeader(E2E_TOKEN, token);
            }

			if (cancellationRequested.get()) {
				// the operation was cancelled before getting it's turn to be executed in the queue of uploads
				result = new RemoteOperationResult(new OperationCancelledException());

			} else {
				// perform the upload
				result = uploadFile(client);
			}

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
			// reset previous retry handler
			client.getParams().setParameter(
				HttpMethodParams.RETRY_HANDLER,
				oldRetryHandler
			);
		}
		return result;
	}

	public boolean isSuccess(int status) {
		return ((status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED ||
                status == HttpStatus.SC_NO_CONTENT));
	}

	protected RemoteOperationResult uploadFile(OwnCloudClient client) throws IOException {
		int status;
		RemoteOperationResult result;

		try {
			File f = new File(localPath);
			entity = new FileRequestEntity(f, mimeType);
			synchronized (dataTransferListeners) {
				((ProgressiveDataTransfer) entity)
                        .addDataTransferProgressListeners(dataTransferListeners);
			}
			if (requiredEtag != null && requiredEtag.length() > 0) {
				putMethod.addRequestHeader(IF_MATCH_HEADER, "\"" + requiredEtag + "\"");
			}
			putMethod.addRequestHeader(OC_TOTAL_LENGTH_HEADER, String.valueOf(f.length()));
            putMethod.addRequestHeader(OC_X_OC_MTIME_HEADER, lastModificationTimestamp);
			putMethod.setRequestEntity(entity);
			status = client.executeMethod(putMethod);

			result = new RemoteOperationResult(isSuccess(status), putMethod);

			client.exhaustResponse(putMethod.getResponseBodyAsStream());

		} finally {
			putMethod.releaseConnection(); // let the connection available for other methods
		}
		return result;
	}

    public Set<OnDatatransferProgressListener> getDataTransferListeners() {
        return dataTransferListeners;
    }
    
    public void addDataTransferProgressListener(OnDatatransferProgressListener listener) {
        synchronized (dataTransferListeners) {
            dataTransferListeners.add(listener);
        }
        if (entity != null) {
            ((ProgressiveDataTransfer) entity).addDataTransferProgressListener(listener);
        }
    }
    
    public void removeDataTransferProgressListener(OnDatatransferProgressListener listener) {
        synchronized (dataTransferListeners) {
            dataTransferListeners.remove(listener);
        }
        if (entity != null) {
            ((ProgressiveDataTransfer) entity).removeDataTransferProgressListener(listener);
        }
    }

    public void cancel(RemoteOperationResult.ResultCode cancellationReason) {
        synchronized (cancellationRequested) {
            cancellationRequested.set(true);

            if (cancellationReason != null) {
                this.cancellationReason = cancellationReason;
            }

            if (putMethod != null) {
                putMethod.abort();
            }
        }
    }

}
