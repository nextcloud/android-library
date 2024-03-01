/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.users;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.DeleteMethod;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;


/**
 * Remote operation performing deletion of app token
 */
public class DeleteAppPasswordRemoteOperation extends RemoteOperation {

    private static final String TAG = DeleteAppPasswordRemoteOperation.class.getSimpleName();
    private static final String URL = "/ocs/v2.php/core/apppassword";

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
            Log_OC.e(TAG, "Deleting of app token failed: " + result.getLogMessage(), result.getException());
        } finally {
            if (deleteMethod != null) {
                deleteMethod.releaseConnection();
            }
        }
        return result;
    }
}
