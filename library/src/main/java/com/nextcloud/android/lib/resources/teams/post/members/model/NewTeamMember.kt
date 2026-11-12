/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.post.members.model

import com.nextcloud.android.lib.resources.teams.model.TeamMemberType

data class NewTeamMember(
    val id: String,
    val type: TeamMemberType
)
