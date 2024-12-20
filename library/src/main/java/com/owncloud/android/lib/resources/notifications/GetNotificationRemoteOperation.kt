/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias.kaminsky@nextcloud.com>
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
import org.json.JSONException
import java.io.IOException

/**
 * Provides the remote notifications from the server handling the following data structure accessible via the
 * notifications endpoint at {@value OCS_ROUTE_LIST_V12_AND_UP}, specified at {@link
 * "https://github.com/nextcloud/notifications/blob/master/docs/ocs-endpoint-v2.md"}.
 */
class GetNotificationRemoteOperation(private val id: Int) : RemoteOperation<Notification>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<Notification> {
        var result: RemoteOperationResult<Notification>
        val status: Int
        var get: GetMethod? = null
        val url = client.baseUri.toString() + OCS_ROUTE_LIST_V12_AND_UP + id + JSON_FORMAT

        // get the notifications
        try {
            get = GetMethod(url, true)
            status = client.execute(get)
            val response = get.getResponseBodyAsString()
            if (isSuccess(status)) {
                result = RemoteOperationResult(true, get)
                Log_OC.d(TAG, "Successful response: $response")

                // Parse the response
                result.setResultData(parseResult(response))
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

    @Throws(JSONException::class)
    private fun parseResult(response: String): Notification {
        val jo = JsonParser.parseString(response) as JsonObject
        val jsonDataObject = jo.getAsJsonObject(NODE_OCS).getAsJsonObject(NODE_DATA)
        val gson = Gson()
        val type = object : TypeToken<Notification>() {}.type

        return gson.fromJson(jsonDataObject, type)
    }

    private fun isSuccess(status: Int): Boolean {
        return status == HttpStatus.SC_OK
    }

    companion object {
        private val TAG = GetNotificationRemoteOperation::class.java.simpleName

        // OCS Route
        private const val OCS_ROUTE_LIST_V12_AND_UP =
            "/ocs/v2.php/apps/notifications/api/v2/notifications/"

        // JSON Node names
        private const val NODE_OCS = "ocs"
        private const val NODE_DATA = "data"
    }
}