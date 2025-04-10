/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.resources.shares.attributes

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

object ShareAttributesJsonHandler {
    private val gson =
        GsonBuilder()
            .registerTypeAdapter(ShareAttributes::class.java, ShareAttributesDeserializer())
            .create()

    fun toList(jsonString: String?): List<ShareAttributes>? {
        if (jsonString == null) {
            return null
        }

        val listType = object : TypeToken<List<ShareAttributes>>() {}.type
        return gson.fromJson(jsonString, listType)
    }

    fun toJson(shareAttributes: List<ShareAttributes>?): String? {
        if (shareAttributes == null) {
            return null
        }

        return gson.toJson(shareAttributes)
    }
}
