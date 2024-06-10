/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.notifications;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.DeleteMethod;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;

/**
 * Delete a notification, specified at
 * {@link "https://github.com/nextcloud/notifications/blob/master/docs/ocs-endpoint-v2.md"}.
 */
public class DeleteNotificationRemoteOperation extends RemoteOperation<Void> {

    // OCS Route
    private static final String OCS_ROUTE_LIST_V12_AND_UP =
            "/ocs/v2.php/apps/notifications/api/v2/notifications/";

    private final int id;

    public DeleteNotificationRemoteOperation(int id) {
        this.id = id;
    }

    @Override
    public RemoteOperationResult<Void> run(NextcloudClient client) {
        RemoteOperationResult<Void> result;
        int status;
        DeleteMethod delete = null;
        String url = client.getBaseUri() + OCS_ROUTE_LIST_V12_AND_UP;

        try {
            delete = new DeleteMethod(url + id, true);

            status = client.execute(delete);
            String response = delete.getResponseBodyAsString();

            if (status == HttpStatus.SC_OK) {
                result = new RemoteOperationResult<>(true, delete);
                Log_OC.d(this, "Successful response: " + response);
            } else {
                result = new RemoteOperationResult<>(false, delete);
                Log_OC.e(this, "Failed response while getting user notifications");
                Log_OC.e(this, "*** status code: " + status + " ;response message: " + response);
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(this, "Exception while getting remote notifications", e);
        } finally {
            if (delete != null) {
                delete.releaseConnection();
            }
        }

        return result;
    }
}
