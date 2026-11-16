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
import com.nextcloud.operations.OcsKotlinRemoteOperation
import com.nextcloud.operations.PostMethod
import com.owncloud.android.lib.ocs.SEPARATOR

class AddTeamMembersRemoteOperation(
    private val teamId: String,
    private val members: List<NewTeamMember>
) : OcsKotlinRemoteOperation<Void>() {
    override fun createMethod(baseUrl: String) =
        PostMethod(
            baseUrl + ENDPOINT + SEPARATOR + teamId + MEMBERS_MULTI + JSON_FORMAT,
            true,
            jsonBody(
                AddTeamMembersRequest(members.map { AddTeamMemberRequest(it.id, it.type.value) }),
                AddTeamMembersRequest.serializer()
            )
        )

    override fun parse(body: String): Void? = null

    companion object {
        private const val ENDPOINT = "/ocs/v2.php/apps/circles/circles"
        private const val MEMBERS_MULTI = "/members/multi"
    }
}
