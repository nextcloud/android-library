/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.collectives.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CollectivesUserFolder(
    @SerialName("user_folder")
    val path: String
)
