/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.post.team

import com.nextcloud.android.lib.resources.teams.post.team.model.CreateTeamRequest
import com.nextcloud.android.lib.resources.teams.model.Team
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.PostMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.OcsKotlinResponse
import com.owncloud.android.lib.ocs.ocsJson
import com.owncloud.android.lib.resources.OCSRemoteOperation
import kotlinx.serialization.encodeToString
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.httpclient.HttpStatus

class CreateTeamRemoteOperation(
    private val name: String,
    private val createTeamFolder: Boolean = true
) : OCSRemoteOperation<Team>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<Team> {
        val request = CreateTeamRequest(name, createTeamFolder)
        val body = ocsJson.encodeToString(request).toRequestBody(JSON_MEDIA_TYPE.toMediaTypeOrNull())
        val postMethod = PostMethod(client.baseUri.toString() + ENDPOINT + JSON_FORMAT, true, body)
        return try {
            val status = client.execute(postMethod)
            if (status != HttpStatus.SC_OK) {
                Log_OC.e(TAG, "Create team failed with status code: $status")
                return RemoteOperationResult(false, postMethod)
            }
            val response =
                ocsJson.decodeFromString<OcsKotlinResponse<Team>>(
                    postMethod.getResponseBodyAsString()
                )
            val data = response.ocs.data
            RemoteOperationResult<Team>(true, postMethod).apply { resultData = data }
        } catch (e: Exception) {
            failure(e)
        } finally {
            postMethod.releaseConnection()
        }
    }

    @Suppress("DEPRECATION")
    private fun failure(e: Exception): RemoteOperationResult<Team> =
        RemoteOperationResult<Team>(e).also {
            Log_OC.e(TAG, "Create team failed: " + it.logMessage, it.exception)
        }

    companion object {
        private val TAG = CreateTeamRemoteOperation::class.java.simpleName
        private const val ENDPOINT = "/ocs/v2.php/apps/circles/circles"
        private const val JSON_MEDIA_TYPE = "application/json"
    }
}
