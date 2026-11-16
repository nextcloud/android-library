/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.get.members

import com.nextcloud.android.lib.resources.teams.model.TeamMember
import com.nextcloud.operations.GetMethod
import com.nextcloud.operations.OcsKotlinRemoteOperation
import com.owncloud.android.lib.ocs.SEPARATOR
import kotlinx.serialization.builtins.ListSerializer

class GetTeamMembersRemoteOperation(
    private val teamId: String
) : OcsKotlinRemoteOperation<List<TeamMember>>() {
    override fun createMethod(baseUrl: String) =
        GetMethod(baseUrl + ENDPOINT + SEPARATOR + teamId + MEMBERS + JSON_FORMAT, true)

    override fun parse(body: String) = decode(body, ListSerializer(TeamMember.serializer()))

    companion object {
        private const val ENDPOINT = "/ocs/v2.php/apps/circles/circles"
        private const val MEMBERS = "/members"
    }
}
