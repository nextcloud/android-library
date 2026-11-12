/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.get.folder

import com.nextcloud.android.lib.resources.teams.post.team.CreateTeamRemoteOperation
import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.status.NextcloudVersion
import org.apache.commons.httpclient.HttpStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetTeamFolderRemoteOperationIT : AbstractIT() {
    @Before
    fun before() {
        testOnlyOnServer(NextcloudVersion.nextcloud_34)
    }

    @Test
    fun getTeamFolder() {
        val created =
            CreateTeamRemoteOperation(TEAM_NAME + System.currentTimeMillis(), true)
                .execute(nextcloudClient)
        assertTrue(created.isSuccess)

        val result = GetTeamFolderRemoteOperation(created.resultData.id).execute(nextcloudClient)

        assertTrue(result.isSuccess)
        assertTrue(result.resultData.mountPoint.isNotEmpty())
    }

    @Test
    fun getTeamFolderWithoutFolder() {
        val created =
            CreateTeamRemoteOperation(TEAM_NAME + System.currentTimeMillis(), false)
                .execute(nextcloudClient)
        assertTrue(created.isSuccess)

        val result = GetTeamFolderRemoteOperation(created.resultData.id).execute(nextcloudClient)

        assertFalse(result.isSuccess)
        assertEquals(HttpStatus.SC_NOT_FOUND, result.httpCode)
    }

    companion object {
        private const val TEAM_NAME = "Team "
    }
}
