/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Your Name <your@email.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.resources.notifications

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.DeleteMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import org.apache.commons.httpclient.util.HttpURLConnection

class UnregisterAccountDeviceForWebPushOperation : RemoteOperation<Void>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<Void> {
        var result: RemoteOperationResult<Void>
        var delete: DeleteMethod? = null
        try {
            delete = DeleteMethod("${client.baseUri}$OCS_ROUTE", true)

            val status = client.execute(delete)
            val response = delete.getResponseBodyAsString()
            when (status) {
                HttpURLConnection.HTTP_ACCEPTED -> {
                    Log_OC.d(TAG, "Web push registration deleted (status=202)")
                    result = RemoteOperationResult(true, delete)
                }

                HttpURLConnection.HTTP_OK -> {
                    Log_OC.d(TAG, "Web push registration already deleted (status=200)")
                    result = RemoteOperationResult(true, delete)
                }

                else -> {
                    Log_OC.e(TAG, "Web push registration refused (status=$status): $response")
                    result = RemoteOperationResult(false, delete)
                }
            }
        } catch (e: Exception) {
            result = RemoteOperationResult(e)
            Log_OC.e(TAG, "Exception while registering web push", e)
        } finally {
            delete?.releaseConnection()
        }
        return result
    }

    companion object {
        // OCS Route
        private const val OCS_ROUTE = "/ocs/v2.php/apps/notifications/api/v2/webpush"

        private val TAG = UnregisterAccountDeviceForWebPushOperation::class.java.getSimpleName()
    }
}
