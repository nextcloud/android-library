/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.put.invitation

import com.nextcloud.android.lib.resources.teams.put.invitation.model.TeamInvitationResponse
import com.nextcloud.operations.OcsKotlinRemoteOperation
import com.nextcloud.operations.PutMethod
import com.owncloud.android.lib.ocs.SEPARATOR
import okhttp3.RequestBody.Companion.toRequestBody

class RespondToTeamInvitationRemoteOperation(
    private val teamId: String,
    private val response: TeamInvitationResponse
) : OcsKotlinRemoteOperation<Void>() {
    override fun createMethod(baseUrl: String) =
        PutMethod(baseUrl + ENDPOINT + SEPARATOR + teamId + response.path + JSON_FORMAT, true, "".toRequestBody())

    override fun parse(body: String): Void? = null

    companion object {
        private const val ENDPOINT = "/ocs/v2.php/apps/circles/circles"
    }
}
