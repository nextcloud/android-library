/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.post.members

import com.nextcloud.android.lib.resources.teams.createTestTeam
import com.nextcloud.android.lib.resources.teams.get.members.GetTeamMembersRemoteOperation
import com.nextcloud.android.lib.resources.teams.model.TeamMemberType
import com.nextcloud.android.lib.resources.teams.post.members.model.NewTeamMember
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
        val team = nextcloudClient.createTestTeam()
        val members = listOf(NewTeamMember(MEMBER_USER_ID, TeamMemberType.USER))

        val result = AddTeamMembersRemoteOperation(team.id, members).execute(nextcloudClient)

        assertTrue(result.isSuccess)
        val teamMembers = GetTeamMembersRemoteOperation(team.id).execute(nextcloudClient)
        assertTrue(teamMembers.resultData.any { it.userId == MEMBER_USER_ID })
    }

    companion object {
        private const val MEMBER_USER_ID = "user1"
    }
}
