/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.get.members

import com.nextcloud.android.lib.resources.teams.createTestTeam
import com.nextcloud.android.lib.resources.teams.model.TeamMemberType
import com.nextcloud.android.lib.resources.teams.model.TeamRole
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
        val team = nextcloudClient.createTestTeam()

        val result = GetTeamMembersRemoteOperation(team.id).execute(nextcloudClient)

        assertTrue(result.isSuccess)
        assertTrue(
            result.resultData.any {
                it.userId == nextcloudClient.userId && it.role == TeamRole.OWNER && it.type == TeamMemberType.USER
            }
        )
    }
}
