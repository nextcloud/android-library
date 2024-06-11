/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.notifications;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.DeleteMethod;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

public class UnregisterAccountDeviceForNotificationsOperation extends RemoteOperation<Void> {

    // OCS Route
    private static final String OCS_ROUTE =
        "/ocs/v2.php/apps/notifications/api/v2/push";

    private static final String TAG = UnregisterAccountDeviceForNotificationsOperation.class.getSimpleName();

    @Override
    public RemoteOperationResult<Void> run(NextcloudClient client) {
        RemoteOperationResult<Void> result;
        int status;
        DeleteMethod delete = null;

        try {
            // Delete Method
            delete = new DeleteMethod(client.getBaseUri() + OCS_ROUTE, true);

            client.execute(delete);
            String response = delete.getResponseBodyAsString();

            if (delete.isSuccess()) {
                result = new RemoteOperationResult<>(true, delete);
                Log_OC.d(TAG, "Successful response: " + response);
            } else {
                result = new RemoteOperationResult<>(false, delete);
            }

        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Exception while registering device for notifications", e);

        } finally {
            if (delete != null) {
                delete.releaseConnection();
            }
        }
        return result;
    }
}
