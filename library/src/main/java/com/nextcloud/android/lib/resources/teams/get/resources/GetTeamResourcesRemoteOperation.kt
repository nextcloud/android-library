/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.get.resources

import com.nextcloud.android.lib.resources.teams.get.resources.model.TeamResource
import com.nextcloud.android.lib.resources.teams.get.resources.model.TeamResourcesResponse
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.OcsKotlinResponse
import com.owncloud.android.lib.ocs.SEPARATOR
import com.owncloud.android.lib.ocs.ocsJson
import com.owncloud.android.lib.resources.OCSRemoteOperation
import org.apache.commons.httpclient.HttpStatus

class GetTeamResourcesRemoteOperation(
    private val teamId: String
) : OCSRemoteOperation<List<TeamResource>>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<List<TeamResource>> {
        val getMethod =
            GetMethod(
                client.baseUri.toString() + ENDPOINT + SEPARATOR + teamId + RESOURCES + JSON_FORMAT,
                true
            )
        return try {
            val status = client.execute(getMethod)
            if (status != HttpStatus.SC_OK) {
                Log_OC.e(TAG, "Get team resources failed with status code: $status")
                return RemoteOperationResult(false, getMethod)
            }
            val response =
                ocsJson.decodeFromString<OcsKotlinResponse<TeamResourcesResponse>>(
                    getMethod.getResponseBodyAsString()
                )
            val data = response.ocs.data.resources
            RemoteOperationResult<List<TeamResource>>(true, getMethod).apply { resultData = data }
        } catch (e: Exception) {
            failure(e)
        } finally {
            getMethod.releaseConnection()
        }
    }

    @Suppress("DEPRECATION")
    private fun failure(e: Exception): RemoteOperationResult<List<TeamResource>> =
        RemoteOperationResult<List<TeamResource>>(e).also {
            Log_OC.e(TAG, "Get team resources failed: " + it.logMessage, it.exception)
        }

    companion object {
        private val TAG = GetTeamResourcesRemoteOperation::class.java.simpleName
        private const val ENDPOINT = "/ocs/v2.php/teams"
        private const val RESOURCES = "/resources"
    }
}
