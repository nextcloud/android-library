/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.post.team

import com.nextcloud.android.lib.resources.teams.model.TeamRole
import com.nextcloud.android.lib.resources.teams.uniqueTeamName
import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.status.NextcloudVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateTeamRemoteOperationIT : AbstractIT() {
    @Before
    fun before() {
        testOnlyOnServer(NextcloudVersion.nextcloud_34)
    }

    @Test
    fun createTeam() {
        val name = uniqueTeamName()

        val result = CreateTeamRemoteOperation(name, false).execute(nextcloudClient)

        assertTrue(result.isSuccess)
        val team = result.resultData
        assertTrue(team.id.isNotEmpty())
        assertEquals(name, team.displayName)
        assertTrue(team.url.isNotEmpty())
        assertEquals(TeamRole.OWNER, team.role)
    }
}
