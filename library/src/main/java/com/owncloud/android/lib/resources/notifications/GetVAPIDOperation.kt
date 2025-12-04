/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Your Name <your@email.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.resources.notifications

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.notifications.models.VapidResponse
import org.json.JSONObject

class GetVAPIDOperation(): RemoteOperation<VapidResponse>() {

    override fun run(client: NextcloudClient): RemoteOperationResult<VapidResponse> {
        var result: RemoteOperationResult<VapidResponse>
        var get: GetMethod? = null
        try {
            get = GetMethod("${client.baseUri}$OCS_ROUTE", true)

            val status = client.execute(get)
            val response = get.getResponseBodyAsString()

            if (get.isSuccess()) {
                result = RemoteOperationResult(true, get)
                val vapid = JSONObject(response)
                    .getJSONObject(OCS)
                    .getJSONObject(DATA)
                    .getString(VAPID)
                result.resultData = VapidResponse(vapid)
                Log_OC.d(TAG, "VAPID key found: $vapid")
            } else {
                Log_OC.e(TAG, "Failed getting VAPID key (status=$status): $response")
                result = RemoteOperationResult(false, get)
            }
        } catch (e: Exception) {
            result = RemoteOperationResult(e)
            Log_OC.e(TAG, "Exception while getting VAPID key", e)
        } finally {
            get?.releaseConnection()
        }
        return result
    }

    companion object {
        // OCS Route
        private const val OCS_ROUTE = "/ocs/v2.php/apps/notifications/api/v2/webpush/vapid$JSON_FORMAT"
        private const val OCS = "ocs"
        private const val DATA = "data"
        private const val VAPID = "vapid"

        private val TAG = GetVAPIDOperation::class.java.getSimpleName()
    }
}