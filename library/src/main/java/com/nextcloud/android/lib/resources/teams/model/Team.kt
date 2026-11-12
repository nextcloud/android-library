/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.model

import kotlinx.serialization.Serializable

@Serializable
data class Team(
    val id: String,
    val name: String,
    val displayName: String,
    val description: String = "",
    val population: Int = 0,
    val url: String = "",
    val initiator: TeamMember? = null
) {
    val isMember: Boolean
        get() = initiator != null && !isPendingRequest

    val isPendingRequest: Boolean
        get() = initiator?.isPendingRequest == true

    val role: TeamRole?
        get() = initiator?.takeIf { isMember }?.role
}
