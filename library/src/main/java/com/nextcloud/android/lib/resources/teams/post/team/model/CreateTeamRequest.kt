/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.post.team.model

import kotlinx.serialization.Serializable

@Serializable
internal data class CreateTeamRequest(
    val name: String,
    val createTeamFolder: Boolean
)
