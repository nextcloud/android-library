/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.post.members

import com.nextcloud.android.lib.resources.teams.post.members.model.AddTeamMemberRequest
import com.nextcloud.android.lib.resources.teams.post.members.model.AddTeamMembersRequest
import com.nextcloud.android.lib.resources.teams.post.members.model.NewTeamMember
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.PostMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.SEPARATOR
import com.owncloud.android.lib.ocs.ocsJson
import com.owncloud.android.lib.resources.OCSRemoteOperation
import kotlinx.serialization.encodeToString
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.httpclient.HttpStatus

class AddTeamMembersRemoteOperation(
    private val teamId: String,
    private val members: List<NewTeamMember>
) : OCSRemoteOperation<Void>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<Void> {
        val request = AddTeamMembersRequest(members.map { AddTeamMemberRequest(it.id, it.type.value) })
        val body = ocsJson.encodeToString(request).toRequestBody(JSON_MEDIA_TYPE.toMediaTypeOrNull())
        val postMethod =
            PostMethod(
                client.baseUri.toString() + ENDPOINT + SEPARATOR + teamId + MEMBERS_MULTI + JSON_FORMAT,
                true,
                body
            )
        return try {
            val status = client.execute(postMethod)
            if (status != HttpStatus.SC_OK) {
                Log_OC.e(TAG, "Add team members failed with status code: $status")
                return RemoteOperationResult(false, postMethod)
            }
            RemoteOperationResult<Void>(true, postMethod)
        } catch (e: Exception) {
            failure(e)
        } finally {
            postMethod.releaseConnection()
        }
    }

    @Suppress("DEPRECATION")
    private fun failure(e: Exception): RemoteOperationResult<Void> =
        RemoteOperationResult<Void>(e).also {
            Log_OC.e(TAG, "Add team members failed: " + it.logMessage, it.exception)
        }

    companion object {
        private val TAG = AddTeamMembersRemoteOperation::class.java.simpleName
        private const val ENDPOINT = "/ocs/v2.php/apps/circles/circles"
        private const val MEMBERS_MULTI = "/members/multi"
        private const val JSON_MEDIA_TYPE = "application/json"
    }
}
