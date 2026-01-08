/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2025 Tobias Kaminsky
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.users

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.OCSRemoteOperation
import okio.IOException
import org.apache.commons.httpclient.HttpStatus
import org.json.JSONObject

/**
 * Generate an app password via username / login and **onetime** password. Available since Nextcloud 33
 */
class GenerateOneTimeAppPasswordRemoteOperation : OCSRemoteOperation<String?>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<String?> {
        var result: RemoteOperationResult<String?>
        var getMethod: GetMethod? = null

        try {
            getMethod = GetMethod(client.baseUri.toString() + ENDPOINT + JSON_FORMAT, true)

            // remote request
            val status: Int = client.execute(getMethod)

            if (status == HttpStatus.SC_OK) {
                val response = getMethod.getResponseBodyAsString()

                val respJSON = JSONObject(response)
                val password =
                    respJSON.getJSONObject(NODE_OCS).getJSONObject(NODE_DATA).getString(
                        NODE_APPPASSWORD
                    )

                result = RemoteOperationResult(true, getMethod)
                result.resultData = password
            } else {
                result = RemoteOperationResult(false, getMethod)
            }
        } catch (e: IOException) {
            result = RemoteOperationResult(e)
            Log_OC.e(
                TAG,
                "Generate app password failed: " + result.getLogMessage(),
                result.exception
            )
        } catch (e: org.json.JSONException) {
            result = RemoteOperationResult(e)
            Log_OC.e(
                TAG,
                "Generate app password failed: " + result.getLogMessage(),
                result.exception
            )
        } finally {
            getMethod?.releaseConnection()
        }
        return result
    }

    companion object {
        private val TAG: String = GenerateOneTimeAppPasswordRemoteOperation::class.java.getSimpleName()
        private const val ENDPOINT = "/ocs/v2.php/core/getapppassword-onetime"

        // JSON node names
        private const val NODE_OCS = "ocs"
        private const val NODE_DATA = "data"
        private const val NODE_APPPASSWORD = "apppassword"
    }
}
