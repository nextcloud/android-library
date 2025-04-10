/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.resources.shares.attributes

data class ShareAttributes(
    val scope: String,
    val key: String,
    var isEnabled: Boolean
)

fun List<ShareAttributes>?.getDownloadAttribute(): ShareAttributes? = this?.find { it.key == "download" }
