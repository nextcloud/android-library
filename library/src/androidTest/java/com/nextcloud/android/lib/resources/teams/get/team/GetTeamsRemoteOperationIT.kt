/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.get.team

import com.nextcloud.android.lib.resources.teams.createTestTeam
import com.nextcloud.android.lib.resources.teams.model.TeamRole
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
        val created = nextcloudClient.createTestTeam()

        val result = GetTeamsRemoteOperation().execute(nextcloudClient)

        assertTrue(result.isSuccess)
        val team = result.resultData.firstOrNull { it.id == created.id }
        assertNotNull(team)
        assertTrue(team!!.isMember)
        assertEquals(TeamRole.OWNER, team.role)
        assertEquals(created.url, team.url)
    }
}
