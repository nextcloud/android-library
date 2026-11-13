/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams

import com.nextcloud.android.lib.resources.teams.model.Team
import com.nextcloud.android.lib.resources.teams.post.team.CreateTeamRemoteOperation
import com.nextcloud.common.NextcloudClient
import org.junit.Assert.assertTrue
import kotlin.uuid.Uuid

private const val TEAM_NAME_PREFIX = "Team "

fun uniqueTeamName(): String = TEAM_NAME_PREFIX + Uuid.random()

fun NextcloudClient.createTestTeam(createTeamFolder: Boolean = false): Team {
    val result = CreateTeamRemoteOperation(uniqueTeamName(), createTeamFolder).execute(this)
    assertTrue(result.isSuccess)
    return result.resultData
}
