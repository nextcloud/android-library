/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias.kaminsky@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.notifications

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.PostMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.notifications.models.Notification
import okhttp3.FormBody
import org.apache.commons.httpclient.HttpStatus
import java.io.IOException

/**
 * Provides the remote notifications from the server handling the following data structure accessible via the
 * notifications endpoint at {@value OCS_ROUTE_LIST_V12_AND_UP}, specified at {@link
 * "https://github.com/nextcloud/notifications/blob/master/docs/ocs-endpoint-v2.md"}.
 */
class CreateNotificationRemoteOperation(private val userId: String, private val message: String) :
    RemoteOperation<Notification>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<Notification> {
        var result: RemoteOperationResult<Notification>
        val status: Int
        var post: PostMethod? = null
        val url =
            client.baseUri.toString() + "/ocs/v2.php/apps/notifications/api/v2/admin_notifications/"

        try {
            val bodyRequest =
                FormBody
                    .Builder()
                    .add("shortMessage", message)
                    .build()

            post = PostMethod(url + userId, true, bodyRequest)
            status = client.execute(post)
            val response = post.getResponseBodyAsString()
            if (isSuccess(status)) {
                result = RemoteOperationResult(true, post)
                Log_OC.d(TAG, "Successful response: $response")
            } else {
                result = RemoteOperationResult(false, post)
                Log_OC.e(TAG, "Failed response while getting user notifications ")
                Log_OC.e(TAG, "*** status code: $status ; response message: $response")
            }
        } catch (e: IOException) {
            result = RemoteOperationResult(e)
            Log_OC.e(TAG, "Exception while getting remote notifications", e)
        } finally {
            post?.releaseConnection()
        }
        return result
    }

    private fun isSuccess(status: Int): Boolean {
        return status == HttpStatus.SC_OK
    }

    companion object {
        private val TAG = CreateNotificationRemoteOperation::class.java.simpleName
    }
}
