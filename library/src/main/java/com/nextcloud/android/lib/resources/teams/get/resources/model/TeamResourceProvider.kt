/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.get.resources.model

import kotlinx.serialization.Serializable

@Serializable
data class TeamResourceProvider(
    val id: String,
    val name: String,
    val icon: String? = null
)
