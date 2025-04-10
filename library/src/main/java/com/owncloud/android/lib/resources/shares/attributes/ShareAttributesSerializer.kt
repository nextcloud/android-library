/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.resources.shares.attributes

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Custom serializer for the ShareAttributes class.
 * This handles the deserialization and serialization of the ShareAttributes data class.
 * Since Nextcloud 30, the enabled key have bee renamed to value and supports more than boolean.
 *
 * https://docs.nextcloud.com/server/latest/developer_manual/client_apis/OCS/ocs-share-api.html#share-attributes
 */
class ShareAttributesDeserializer : JsonDeserializer<ShareAttributes> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ShareAttributes? {
        val jsonObject = json?.asJsonObject
        val scope = jsonObject?.get("scope")?.asString ?: ""
        val key = jsonObject?.get("key")?.asString ?: ""

        val status = when {
            jsonObject?.has("enabled") == true -> jsonObject.get("enabled").asBoolean
            jsonObject?.has("value") == true -> jsonObject.get("value").asBoolean
            else -> false
        }

        return ShareAttributes(scope, key, status)
    }
}
