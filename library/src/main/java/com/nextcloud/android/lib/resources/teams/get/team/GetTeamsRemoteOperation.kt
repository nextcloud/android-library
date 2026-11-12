/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.get.team

import com.nextcloud.android.lib.resources.teams.model.Team
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.OcsKotlinResponse
import com.owncloud.android.lib.ocs.ocsJson
import com.owncloud.android.lib.resources.OCSRemoteOperation
import org.apache.commons.httpclient.HttpStatus

class GetTeamsRemoteOperation : OCSRemoteOperation<List<Team>>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<List<Team>> {
        val getMethod = GetMethod(client.baseUri.toString() + ENDPOINT + JSON_FORMAT, true)
        return try {
            val status = client.execute(getMethod)
            if (status != HttpStatus.SC_OK) {
                Log_OC.e(TAG, "Get teams failed with status code: $status")
                return RemoteOperationResult(false, getMethod)
            }
            val response =
                ocsJson.decodeFromString<OcsKotlinResponse<List<Team>>>(
                    getMethod.getResponseBodyAsString()
                )
            val data = response.ocs.data
            RemoteOperationResult<List<Team>>(true, getMethod).apply { resultData = data }
        } catch (e: Exception) {
            failure(e)
        } finally {
            getMethod.releaseConnection()
        }
    }

    @Suppress("DEPRECATION")
    private fun failure(e: Exception): RemoteOperationResult<List<Team>> =
        RemoteOperationResult<List<Team>>(e).also {
            Log_OC.e(TAG, "Get teams failed: " + it.logMessage, it.exception)
        }

    companion object {
        private val TAG = GetTeamsRemoteOperation::class.java.simpleName
        private const val ENDPOINT = "/ocs/v2.php/apps/circles/circles"
    }
}
