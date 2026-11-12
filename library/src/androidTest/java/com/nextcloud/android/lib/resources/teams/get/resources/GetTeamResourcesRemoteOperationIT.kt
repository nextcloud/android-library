/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.get.resources

import com.nextcloud.android.lib.resources.teams.post.team.CreateTeamRemoteOperation
import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.status.NextcloudVersion
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetTeamResourcesRemoteOperationIT : AbstractIT() {
    @Before
    fun before() {
        testOnlyOnServer(NextcloudVersion.nextcloud_34)
    }

    @Test
    fun getResources() {
        val created =
            CreateTeamRemoteOperation(TEAM_NAME + System.currentTimeMillis(), false)
                .execute(nextcloudClient)
        assertTrue(created.isSuccess)

        val result = GetTeamResourcesRemoteOperation(created.resultData.id).execute(nextcloudClient)

        assertTrue(result.isSuccess)
        assertNotNull(result.resultData)
    }

    companion object {
        private const val TEAM_NAME = "Team "
    }
}
