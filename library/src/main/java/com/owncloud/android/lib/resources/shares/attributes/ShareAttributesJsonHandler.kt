/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.resources.shares.attributes

import kotlinx.serialization.json.Json

object ShareAttributesJsonHandler {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Parses a JSON string into a list of `ShareAttributes` objects.
     */
    fun parseJson(jsonString: String): List<ShareAttributes> = json.decodeFromString(jsonString)

    /**
     * Converts a list of `ShareAttributes` objects into a JSON string.
     */
    fun toJson(shareAttributes: List<ShareAttributes>): String = json.encodeToString(shareAttributes)
}
