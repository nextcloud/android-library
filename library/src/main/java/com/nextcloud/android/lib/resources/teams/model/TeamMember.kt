/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.model

import kotlinx.serialization.Serializable

@Serializable
data class TeamMember(
    val singleId: String,
    val userId: String? = null,
    val displayName: String = "",
    val userType: Int = 0,
    val level: Int = 0,
    val status: String = ""
) {
    val role: TeamRole
        get() = TeamRole.fromLevel(level)

    val type: TeamMemberType?
        get() = TeamMemberType.fromValue(userType)

    val isPendingRequest: Boolean
        get() = status == STATUS_REQUESTING

    companion object {
        const val STATUS_REQUESTING = "Requesting"
    }
}
