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
    var value: Boolean
) {
    companion object {
        const val DOWNLOAD_ATTRIBUTE_KEY = "download"

        fun createDownloadAttributes(value: Boolean): ShareAttributes =
            ShareAttributes(scope = "permissions", key = DOWNLOAD_ATTRIBUTE_KEY, value = value)
    }
}

fun List<ShareAttributes>?.getDownloadAttribute(): ShareAttributes? =
    this?.find { it.key == ShareAttributes.DOWNLOAD_ATTRIBUTE_KEY }
