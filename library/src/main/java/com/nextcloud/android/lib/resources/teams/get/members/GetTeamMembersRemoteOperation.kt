/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.get.members

import com.nextcloud.android.lib.resources.teams.model.TeamMember
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.OcsKotlinResponse
import com.owncloud.android.lib.ocs.SEPARATOR
import com.owncloud.android.lib.ocs.ocsJson
import com.owncloud.android.lib.resources.OCSRemoteOperation
import org.apache.commons.httpclient.HttpStatus

class GetTeamMembersRemoteOperation(
    private val teamId: String
) : OCSRemoteOperation<List<TeamMember>>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<List<TeamMember>> {
        val getMethod =
            GetMethod(
                client.baseUri.toString() + ENDPOINT + SEPARATOR + teamId + MEMBERS + JSON_FORMAT,
                true
            )
        return try {
            val status = client.execute(getMethod)
            if (status != HttpStatus.SC_OK) {
                Log_OC.e(TAG, "Get team members failed with status code: $status")
                return RemoteOperationResult(false, getMethod)
            }
            val response =
                ocsJson.decodeFromString<OcsKotlinResponse<List<TeamMember>>>(
                    getMethod.getResponseBodyAsString()
                )
            val data = response.ocs.data
            RemoteOperationResult<List<TeamMember>>(true, getMethod).apply { resultData = data }
        } catch (e: Exception) {
            failure(e)
        } finally {
            getMethod.releaseConnection()
        }
    }

    @Suppress("DEPRECATION")
    private fun failure(e: Exception): RemoteOperationResult<List<TeamMember>> =
        RemoteOperationResult<List<TeamMember>>(e).also {
            Log_OC.e(TAG, "Get team members failed: " + it.logMessage, it.exception)
        }

    companion object {
        private val TAG = GetTeamMembersRemoteOperation::class.java.simpleName
        private const val ENDPOINT = "/ocs/v2.php/apps/circles/circles"
        private const val MEMBERS = "/members"
    }
}
