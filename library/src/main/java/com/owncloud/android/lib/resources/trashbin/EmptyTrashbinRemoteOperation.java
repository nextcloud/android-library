/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2018 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.trashbin;

import android.util.Log;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.DeleteMethod;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;

import org.apache.commons.httpclient.HttpStatus;

import java.io.IOException;

/**
 * Empty trashbin.
 */
public class EmptyTrashbinRemoteOperation extends RemoteOperation<Boolean> {

    private static final String TAG = EmptyTrashbinRemoteOperation.class.getSimpleName();

    /**
     * Performs the operation.
     *
     * @param client Client object to communicate with the remote Nextcloud server.
     */
    @Override
    public RemoteOperationResult<Boolean> run(NextcloudClient client) {

        DeleteMethod delete = null;
        RemoteOperationResult<Boolean> result;
        try {
            delete = new DeleteMethod(
                    client.getDavUri() + "/trashbin/" + client.getUserId() + "/trash",
                    true);
            int status = client.execute(delete);

            result = new RemoteOperationResult<>(isSuccess(status), delete);
        } catch (IOException e) {
            result = new RemoteOperationResult<>(e);
            Log.e(TAG, "Empty trashbin failed: " + result.getLogMessage(), e);
        } finally {
            if (delete != null) {
                delete.releaseConnection();
            }
        }

        return result;
    }

    private boolean isSuccess(int status) {
        return status == HttpStatus.SC_NO_CONTENT;
    }
}
