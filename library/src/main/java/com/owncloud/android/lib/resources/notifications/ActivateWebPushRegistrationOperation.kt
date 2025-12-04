/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Your Name <your@email.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.resources.notifications

import com.nextcloud.common.JSONRequestBody
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.PostMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import org.apache.commons.httpclient.util.HttpURLConnection

class ActivateWebPushRegistrationOperation(
    val activationToken: String
): RemoteOperation<Void>() {

    override fun run(client: NextcloudClient): RemoteOperationResult<Void> {
        var result: RemoteOperationResult<Void>
        var post: PostMethod? = null
        try {
            val body = JSONRequestBody(ACTIVATION_TOKEN, activationToken)
            post = PostMethod("${client.baseUri}$OCS_ROUTE", true, body.get())

            val status = client.execute(post)
            val response = post.getResponseBodyAsString()
            when (status) {
                HttpURLConnection.HTTP_ACCEPTED -> {
                    Log_OC.d(TAG, "Web push registration activated (status=202)")
                    result = RemoteOperationResult(true, post)
                }
                HttpURLConnection.HTTP_OK -> {
                    Log_OC.d(TAG, "Web push registration already activated (status=200)")
                    result = RemoteOperationResult(true, post)
                }
                else -> {
                    Log_OC.d(TAG, "Cannot activate web push registration (status=$status): $response")
                    result = RemoteOperationResult(false, post)
                }
            }
        } catch (e: Exception) {
            result = RemoteOperationResult(e)
            Log_OC.e(TAG, "Exception while activating web push registration", e)
        } finally {
            post?.releaseConnection()
        }
        return result
    }

    companion object {
        // OCS Route
        private const val OCS_ROUTE = "/ocs/v2.php/apps/notifications/api/v2/webpush/activate"
        private const val ACTIVATION_TOKEN = "activationToken"

        private val TAG = ActivateWebPushRegistrationOperation::class.java.getSimpleName()
    }
}