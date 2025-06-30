/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.shares.attributes

open class ShareAttributes(
    open val scope: String,
    open val key: String,
    open var abstractValue: Boolean
) {
    companion object {
        const val DOWNLOAD_ATTRIBUTE_KEY = "download"

        fun createDownloadAttributes(value: Boolean, useV2: Boolean): ShareAttributes =
            if (useV2) {
                ShareAttributesV2(scope = "permissions", key = DOWNLOAD_ATTRIBUTE_KEY, enabled = value)
            } else {
                ShareAttributesV1(scope = "permissions", key = DOWNLOAD_ATTRIBUTE_KEY, value = value)
            }
    }

    private data class ShareAttributesV1(
        override val scope: String,
        override val key: String,
        val value: Boolean
    ) : ShareAttributes(scope, key, value)

    private data class ShareAttributesV2(
        override val scope: String,
        override val key: String,
        val enabled: Boolean
    ) : ShareAttributes(scope, key, enabled)
}

fun List<ShareAttributes>?.getDownloadAttribute(): ShareAttributes? =
    this?.find { it.key == ShareAttributes.DOWNLOAD_ATTRIBUTE_KEY }
