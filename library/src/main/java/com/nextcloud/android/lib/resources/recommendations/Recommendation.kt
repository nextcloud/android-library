/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.recommendations

data class Recommendation(
    val id: Long,
    val timestamp: Long,
    val name: String,
    val directory: String,
    val extension: String,
    val mimeType: String,
    val hasPreview: Boolean,
    val reason: String
)
