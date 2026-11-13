/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.post.team

import com.nextcloud.android.lib.resources.teams.model.Team
import com.nextcloud.android.lib.resources.teams.post.team.model.CreateTeamRequest
import com.nextcloud.operations.OcsKotlinRemoteOperation
import com.nextcloud.operations.PostMethod

class CreateTeamRemoteOperation(
    private val name: String,
    private val createTeamFolder: Boolean = true
) : OcsKotlinRemoteOperation<Team>() {
    override fun createMethod(baseUrl: String) =
        PostMethod(
            baseUrl + ENDPOINT + JSON_FORMAT,
            true,
            jsonBody(CreateTeamRequest(name, createTeamFolder), CreateTeamRequest.serializer())
        )

    override fun parse(body: String) = decode(body, Team.serializer())

    companion object {
        private const val ENDPOINT = "/ocs/v2.php/apps/circles/circles"
    }
}
