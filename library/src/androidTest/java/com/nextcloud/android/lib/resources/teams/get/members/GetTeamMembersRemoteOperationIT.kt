/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.get.members

import com.nextcloud.android.lib.resources.teams.model.TeamMemberType
import com.nextcloud.android.lib.resources.teams.model.TeamRole
import com.nextcloud.android.lib.resources.teams.post.team.CreateTeamRemoteOperation
import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.status.NextcloudVersion
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetTeamMembersRemoteOperationIT : AbstractIT() {
    @Before
    fun before() {
        testOnlyOnServer(NextcloudVersion.nextcloud_34)
    }

    @Test
    fun getMembers() {
        val created =
            CreateTeamRemoteOperation(TEAM_NAME + System.currentTimeMillis(), false)
                .execute(nextcloudClient)
        assertTrue(created.isSuccess)

        val result = GetTeamMembersRemoteOperation(created.resultData.id).execute(nextcloudClient)

        assertTrue(result.isSuccess)
        assertTrue(
            result.resultData.any {
                it.userId == nextcloudClient.userId && it.role == TeamRole.OWNER && it.type == TeamMemberType.USER
            }
        )
    }

    companion object {
        private const val TEAM_NAME = "Team "
    }
}
