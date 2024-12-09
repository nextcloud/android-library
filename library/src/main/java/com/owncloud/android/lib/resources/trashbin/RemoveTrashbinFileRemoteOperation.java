/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2018 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.trashbin;

import com.nextcloud.common.SessionTimeOut;
import com.nextcloud.common.SessionTimeOutKt;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.network.WebdavUtils;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;

/**
 * Remote operation performing the removal of a file in trashbin.
 */
public class RemoveTrashbinFileRemoteOperation extends RemoteOperation {
    private static final String TAG = RemoveTrashbinFileRemoteOperation.class.getSimpleName();

    private final String remotePath;
    private final SessionTimeOut sessionTimeOut;

    /**
     * Constructor
     *
     * @param remotePath RemotePath of the remote file or folder to remove from the server
     */
    public RemoveTrashbinFileRemoteOperation(String remotePath) {
        this(remotePath, SessionTimeOutKt.getDefaultSessionTimeOut());
    }

    public RemoveTrashbinFileRemoteOperation(String remotePath, SessionTimeOut sessionTimeOut) {
        this.remotePath = remotePath;
        this.sessionTimeOut = sessionTimeOut;
    }

    /**
     * Performs the remove operation.
     *
     * @param client Client object to communicate with the remote ownCloud server.
     */
    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result;
        DeleteMethod delete = null;

        try {
            delete = new DeleteMethod(client.getDavUri() + WebdavUtils.encodePath(remotePath));
            int status = client.executeMethod(delete, sessionTimeOut.getReadTimeOut(), sessionTimeOut.getConnectionTimeOut());

            delete.getResponseBodyAsString();   // exhaust the response, although not interesting
            result = new RemoteOperationResult<>((delete.succeeded() || status == HttpStatus.SC_NOT_FOUND), delete);
            Log_OC.i(TAG, "Remove " + remotePath + ": " + result.getLogMessage());

        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Remove " + remotePath + ": " + result.getLogMessage(), e);

        } finally {
            if (delete != null)
                delete.releaseConnection();
        }

        return result;
    }
}
