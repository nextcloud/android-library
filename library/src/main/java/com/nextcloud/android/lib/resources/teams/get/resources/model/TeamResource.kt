/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.get.resources.model

import kotlinx.serialization.Serializable

@Serializable
data class TeamResource(
    val id: String,
    val label: String,
    val url: String,
    val iconSvg: String? = null,
    val iconURL: String? = null,
    val iconEmoji: String? = null,
    val provider: TeamResourceProvider
)
