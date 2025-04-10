/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.resources.shares.attributes

import kotlinx.serialization.Serializable

/**
 * Share attributes are used for more advanced flags like permissions.
 *
 *
 * https://docs.nextcloud.com/server/latest/developer_manual/client_apis/OCS/ocs-share-api.html#share-attributes
 */
@Serializable(with = ShareAttributesSerializer::class)
data class ShareAttributes(
    val scope: String,
    val key: String,
    var isEnabled: Boolean
)

fun List<ShareAttributes>?.getDownloadAttribute(): ShareAttributes? = this?.find { it.key == "download" }
