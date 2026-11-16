/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.post.members.model

import kotlinx.serialization.Serializable

@Serializable
internal data class AddTeamMembersRequest(
    val members: List<AddTeamMemberRequest>
)
