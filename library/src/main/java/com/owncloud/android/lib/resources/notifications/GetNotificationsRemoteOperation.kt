/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-FileCopyrightText: 2017 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.notifications

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.notifications.models.Notification
import org.apache.commons.httpclient.HttpStatus
import java.io.IOException

/**
 * Provides the remote notifications from the server handling the following data structure
 * accessible via the notifications endpoint at {@value OCS_ROUTE_LIST_V12_AND_UP}, specified at
 * {@link "https://github.com/nextcloud/notifications/blob/master/docs/ocs-endpoint-v2.md"}.
 */
class GetNotificationsRemoteOperation : RemoteOperation<List<Notification>>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<List<Notification>> {
        var result: RemoteOperationResult<List<Notification>>
        val status: Int
        var get: GetMethod? = null
        val notifications: List<Notification>
        val url = client.baseUri.toString() + OCS_ROUTE_LIST_V12_AND_UP

        // get the notifications
        try {
            get = GetMethod(url, true)
            status = client.execute(get)
            val response = get.getResponseBodyAsString()
            if (isSuccess(status)) {
                result = RemoteOperationResult(true, get)
                Log_OC.d(TAG, "Successful response: $response")

                // Parse the response
                notifications = parseResult(response)
                result.setResultData(notifications)
            } else {
                result = RemoteOperationResult(false, get)
                Log_OC.e(TAG, "Failed response while getting user notifications ")
                Log_OC.e(TAG, "*** status code: $status ; response message: $response")
            }
        } catch (e: IOException) {
            result = RemoteOperationResult(e)
            Log_OC.e(TAG, "Exception while getting remote notifications", e)
        } finally {
            get?.releaseConnection()
        }
        return result
    }

    private fun parseResult(response: String): List<Notification> {
        val jo = JsonParser.parseString(response) as JsonObject
        val jsonDataArray = jo.getAsJsonObject(NODE_OCS).getAsJsonArray(NODE_DATA)
        val gson = Gson()
        val listType = object : TypeToken<List<Notification?>?>() {}.type
        return gson.fromJson(jsonDataArray, listType)
    }

    private fun isSuccess(status: Int): Boolean {
        return status == HttpStatus.SC_OK
    }

    companion object {
        // OCS Route
        private const val OCS_ROUTE_LIST_V12_AND_UP =
            "/ocs/v2.php/apps/notifications/api/v2/notifications$JSON_FORMAT"
        private val TAG = GetNotificationsRemoteOperation::class.java.simpleName

        // JSON Node names
        private const val NODE_OCS = "ocs"
        private const val NODE_DATA = "data"
    }
}
