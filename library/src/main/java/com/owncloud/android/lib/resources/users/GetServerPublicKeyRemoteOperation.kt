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
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<String> {
        val getMethod = GetMethod(client.baseUri.toString() + ENDPOINT + JSON_FORMAT, true)

        return try {
            val status = client.execute(getMethod)
            if (status != HttpStatus.SC_OK) {
                return RemoteOperationResult(false, getMethod)
            }
            val serverKey =
                JSONObject(getMethod.getResponseBodyAsString())
                    .getJSONObject(NODE_OCS)
                    .getJSONObject(NODE_DATA)
                    .getString(NODE_PUBLIC_KEY)
            RemoteOperationResult<String>(true, getMethod).apply { resultData = serverKey }
        } catch (e: Exception) {
            failure(e, client.userId)
        } finally {
            getMethod.releaseConnection()
        }
    }

    @Suppress("DEPRECATION")
    private fun failure(
        e: Exception,
        user: String
    ): RemoteOperationResult<String> =
        RemoteOperationResult<String>(e).also {
            Log_OC.e(TAG, "Fetching of server public key failed for user $user: ${it.logMessage}", it.exception)
        }

    companion object {
        private val TAG = GetServerPublicKeyRemoteOperation::class.java.simpleName
        private const val ENDPOINT = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/server-key"
        private const val NODE_OCS = "ocs"
        private const val NODE_DATA = "data"
        private const val NODE_PUBLIC_KEY = "public-key"
    }
}
