/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.teams.get.folder.model

import kotlinx.serialization.Serializable

@Serializable
data class TeamFolder(
    val id: Long,
    val mountPoint: String,
    val quota: Long? = null
)
