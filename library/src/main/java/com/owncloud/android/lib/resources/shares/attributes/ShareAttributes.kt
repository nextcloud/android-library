/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Your Name <your@email.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.resources.shares.attributes

import kotlinx.serialization.Serializable

@Serializable(with = ShareAttributesSerializer::class)
data class ShareAttributes(
    val scope: String,
    val key: String,
    var isEnabled: Boolean
)

fun List<ShareAttributes>?.getDownloadAttribute(): ShareAttributes? {
    return this?.find { it.key == "download" }
}
