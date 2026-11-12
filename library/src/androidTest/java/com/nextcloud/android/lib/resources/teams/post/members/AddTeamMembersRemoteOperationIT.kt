/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.post.members

import com.nextcloud.android.lib.resources.teams.get.members.GetTeamMembersRemoteOperation
import com.nextcloud.android.lib.resources.teams.model.TeamMemberType
import com.nextcloud.android.lib.resources.teams.post.members.model.NewTeamMember
import com.nextcloud.android.lib.resources.teams.post.team.CreateTeamRemoteOperation
import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.status.NextcloudVersion
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddTeamMembersRemoteOperationIT : AbstractIT() {
    @Before
    fun before() {
        testOnlyOnServer(NextcloudVersion.nextcloud_34)
    }

    @Test
    fun addMembers() {
        val created =
            CreateTeamRemoteOperation(TEAM_NAME + System.currentTimeMillis(), false)
                .execute(nextcloudClient)
        assertTrue(created.isSuccess)
        val teamId = created.resultData.id
        val members = listOf(NewTeamMember(MEMBER_USER_ID, TeamMemberType.USER))

        val result = AddTeamMembersRemoteOperation(teamId, members).execute(nextcloudClient)

        assertTrue(result.isSuccess)
        val teamMembers = GetTeamMembersRemoteOperation(teamId).execute(nextcloudClient)
        assertTrue(teamMembers.resultData.any { it.userId == MEMBER_USER_ID })
    }

    companion object {
        private const val TEAM_NAME = "Team "
        private const val MEMBER_USER_ID = "user1"
    }
}
