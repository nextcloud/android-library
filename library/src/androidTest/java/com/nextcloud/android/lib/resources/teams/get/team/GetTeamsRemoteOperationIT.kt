/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.get.team

import com.nextcloud.android.lib.resources.teams.model.TeamRole
import com.nextcloud.android.lib.resources.teams.post.team.CreateTeamRemoteOperation
import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.status.NextcloudVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetTeamsRemoteOperationIT : AbstractIT() {
    @Before
    fun before() {
        testOnlyOnServer(NextcloudVersion.nextcloud_34)
    }

    @Test
    fun getTeams() {
        val created =
            CreateTeamRemoteOperation(TEAM_NAME + System.currentTimeMillis(), false)
                .execute(nextcloudClient)
        assertTrue(created.isSuccess)

        val result = GetTeamsRemoteOperation().execute(nextcloudClient)

        assertTrue(result.isSuccess)
        val team = result.resultData.firstOrNull { it.id == created.resultData.id }
        assertNotNull(team)
        assertTrue(team!!.isMember)
        assertEquals(TeamRole.OWNER, team.role)
        assertEquals(created.resultData.url, team.url)
    }

    companion object {
        private const val TEAM_NAME = "Team "
    }
}
