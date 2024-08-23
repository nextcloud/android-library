/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.users

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import org.apache.commons.httpclient.HttpStatus
import org.json.JSONObject

class GetServerPublicKeyRemoteOperation : RemoteOperation<String>() {
    companion object {
        private val TAG: String = GetPublicKeyRemoteOperation::class.java.simpleName
        private const val ENDPOINT = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/server-key"
    }

    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<String> {
        var getMethod: GetMethod? = null
        var result: RemoteOperationResult<String>
        val user: String = client.userId

        try {
            getMethod = GetMethod(client.baseUri.toString() + ENDPOINT + JSON_FORMAT, true)

            val status = client.execute(getMethod)

            if (status == HttpStatus.SC_OK) {
                val response = getMethod.getResponseBodyAsString()
                val respJSON = JSONObject(response)
                val serverKey = respJSON.getJSONObject("ocs").getJSONObject("data").getString("public-key")

                result = RemoteOperationResult(true, getMethod)
                result.setResultData(serverKey)
            } else {
                result = RemoteOperationResult(false, getMethod)
            }
        } catch (e: Exception) {
            result = RemoteOperationResult(e)
            Log_OC.e(
                TAG,
                "Fetching of server public key failed for user " + user + ": " + result.logMessage,
                result.exception
            )
        } finally {
            getMethod?.releaseConnection()
        }

        return result
    }
}
