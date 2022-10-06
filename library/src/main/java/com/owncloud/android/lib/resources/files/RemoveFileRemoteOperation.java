/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.DeleteMethod;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import okhttp3.internal.http.HttpStatusCodesKt;

/**
 * Remote operation performing the removal of a remote file or folder in the ownCloud server.
 *
 * @author David A. Velasco
 * @author masensio
 */
public class RemoveFileRemoteOperation extends RemoteOperation<Void> {
    private static final String TAG = RemoveFileRemoteOperation.class.getSimpleName();

    private String mRemotePath;

    /**
     * Constructor
     *
     * @param remotePath RemotePath of the remote file or folder to remove from the server
     */
    public RemoveFileRemoteOperation(String remotePath) {
        mRemotePath = remotePath;
    }

    /**
     * Performs the rename operation.
     *
     * @param client Client object to communicate with the remote ownCloud server.
     */
    @Override
    public RemoteOperationResult<Void> run(NextcloudClient client) {
        RemoteOperationResult<Void> result;
        DeleteMethod delete = null;

        try {
            delete = new DeleteMethod(client.getFilesDavUri(mRemotePath), true);
            int status = client.execute(delete);

            result = new RemoteOperationResult<>(
                (delete.isSuccess() || status == HttpStatusCodesKt.HTTP_NOT_FOUND || status == HttpStatusCodesKt.HTTP_NO_CONTENT),
                delete
            );
            Log_OC.i(TAG, "Remove " + mRemotePath + ": " + result.getLogMessage());

        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Remove " + mRemotePath + ": " + result.getLogMessage(), e);

        } finally {
            if (delete != null) {
                delete.releaseConnection();
            }
        }

        return result;
    }

}
