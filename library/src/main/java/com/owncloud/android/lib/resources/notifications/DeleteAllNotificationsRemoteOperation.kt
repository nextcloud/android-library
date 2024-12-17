/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.notifications

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.DeleteMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import java.io.IOException

/**
 * Delete all notification, specified at
 * {@link "https://github.com/nextcloud/notifications/blob/master/docs/ocs-endpoint-v2.md"}.
 */
class DeleteAllNotificationsRemoteOperation : RemoteOperation<Void>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<Void> {
        var result: RemoteOperationResult<Void>
        val status: Int
        var delete: DeleteMethod? = null
        val url = client.baseUri.toString() + OCS_ROUTE_LIST_V12_AND_UP
        try {
            delete = DeleteMethod(url, true)

            status = client.execute(delete)
            val response = delete.getResponseBodyAsString()

            if (delete.isSuccess()) {
                result = RemoteOperationResult(true, delete)
                Log_OC.d(this, "Successful response: $response")
            } else {
                result = RemoteOperationResult(false, delete)
                Log_OC.e(this, "Failed response while deleting all user notifications")
                Log_OC.e(this, "*** status code: $status ;response message: $response")
            }
        } catch (e: IOException) {
            result = RemoteOperationResult(e)
            Log_OC.e(this, "Exception while getting remote notifications", e)
        } finally {
            delete?.releaseConnection()
        }
        return result
    }

    companion object {
        // OCS Route
        private const val OCS_ROUTE_LIST_V12_AND_UP =
            "/ocs/v2.php/apps/notifications/api/v2/notifications"
    }
}
