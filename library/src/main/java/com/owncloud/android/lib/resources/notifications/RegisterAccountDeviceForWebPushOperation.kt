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

class RegisterAccountDeviceForWebPushOperation(
    val endpoint: String,
    val auth: String,
    val uaPublicKey: String,
    val appTypes: List<String>
): RemoteOperation<Void>() {

    override fun run(client: NextcloudClient): RemoteOperationResult<Void> {
        var result: RemoteOperationResult<Void>
        var post: PostMethod? = null
        try {
            val body = JSONRequestBody(ENDPOINT, endpoint)
            body.put(AUTH, auth)
            body.put(UA_PUBLIC_KEY, uaPublicKey)
            body.put(APPTYPES, appTypes.joinToString(","))
            post = PostMethod("${client.baseUri}$OCS_ROUTE", true, body.get())

            val status = client.execute(post)
            val response = post.getResponseBodyAsString()
            when (status) {
                HttpURLConnection.HTTP_CREATED -> {
                    Log_OC.d(TAG, "New web push registration created (status=201)")
                    result = RemoteOperationResult(true, post)
                }
                HttpURLConnection.HTTP_OK -> {
                    Log_OC.d(TAG, "Web push registration already activated (status=200)")
                    result = RemoteOperationResult(true, post)
                }
                else -> {
                    Log_OC.e(TAG, "Web push registration refused (status=$status): $response")
                    result = RemoteOperationResult(false, post)
                }
            }
        } catch (e: Exception) {
            result = RemoteOperationResult(e)
            Log_OC.e(TAG, "Exception while registering web push", e)
        } finally {
            post?.releaseConnection()
        }
        return result
    }

    companion object {
        // OCS Route
        private const val OCS_ROUTE = "/ocs/v2.php/apps/notifications/api/v2/webpush"
        private const val ENDPOINT = "endpoint"
        private const val AUTH = "auth"
        private const val UA_PUBLIC_KEY = "uaPublicKey"
        private const val APPTYPES = "appTypes"

        private val TAG = RegisterAccountDeviceForWebPushOperation::class.java.getSimpleName()
    }
}