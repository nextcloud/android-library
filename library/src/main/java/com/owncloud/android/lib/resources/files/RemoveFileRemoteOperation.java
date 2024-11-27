/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files;

import com.nextcloud.common.SessionTimeOut;
import com.nextcloud.common.SessionTimeOutKt;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;

/**
 * Remote operation performing the removal of a remote file or folder in the ownCloud server.
 *
 * @author David A. Velasco
 * @author masensio
 */
public class RemoveFileRemoteOperation extends RemoteOperation {
    private static final String TAG = RemoveFileRemoteOperation.class.getSimpleName();

    private final String mRemotePath;
    private final SessionTimeOut sessionTimeOut;

    /**
     * Constructor
     *
     * @param remotePath RemotePath of the remote file or folder to remove from the server
     */
    public RemoveFileRemoteOperation(String remotePath) {
        mRemotePath = remotePath;
        sessionTimeOut = SessionTimeOutKt.getDefaultSessionTimeOut();
    }

    public RemoveFileRemoteOperation(String remotePath, SessionTimeOut sessionTimeOut) {
        mRemotePath = remotePath;
        this.sessionTimeOut = sessionTimeOut;
    }

    /**
     * Performs the rename operation.
     *
     * @param client Client object to communicate with the remote ownCloud server.
     */
    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result = null;
        DeleteMethod delete = null;

        try {
            delete = new DeleteMethod(client.getFilesDavUri(mRemotePath));
            int status = client.executeMethod(delete, sessionTimeOut.getReadTimeOut(), sessionTimeOut.getConnectionTimeOut());

            delete.getResponseBodyAsString();   // exhaust the response, although not interesting
            result = new RemoteOperationResult<>(
                (delete.succeeded() || status == HttpStatus.SC_NOT_FOUND),
                delete
            );
            Log_OC.i(TAG, "Remove " + mRemotePath + ": " + result.getLogMessage());

        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Remove " + mRemotePath + ": " + result.getLogMessage(), e);

        } finally {
            if (delete != null)
                delete.releaseConnection();
        }

        return result;
    }
}
