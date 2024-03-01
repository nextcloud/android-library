/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.users;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.DeleteMethod;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.OCSRemoteOperation;

import org.apache.commons.httpclient.HttpStatus;


/**
 * Remote operation to clear custome status message
 */

public class ClearStatusMessageRemoteOperation extends OCSRemoteOperation {

    private static final String TAG = ClearStatusMessageRemoteOperation.class.getSimpleName();
    private static final String URL = "/ocs/v2.php/apps/user_status/api/v1/user_status/message";

    /**
     * @param client Client object
     */
    @Override
    public RemoteOperationResult run(NextcloudClient client) {
        DeleteMethod deleteMethod = null;
        RemoteOperationResult result;

        try {
            // remote request
            deleteMethod = new DeleteMethod(client.getBaseUri() + URL + JSON_FORMAT, true);

            int status = client.execute(deleteMethod);

            if (status == HttpStatus.SC_OK) {
                result = new RemoteOperationResult(true, deleteMethod);
            } else {
                result = new RemoteOperationResult(false, deleteMethod);
                deleteMethod.releaseConnection();
            }
        } catch (Exception e) {
            result = new RemoteOperationResult(e);
            Log_OC.e(TAG, "Deleting of own status message failed: " + result.getLogMessage(), result.getException());
        } finally {
            if (deleteMethod != null) {
                deleteMethod.releaseConnection();
            }
        }
        return result;
    }
}
