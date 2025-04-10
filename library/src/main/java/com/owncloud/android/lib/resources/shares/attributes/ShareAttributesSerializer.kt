/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Your Name <your@email.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.resources.shares.attributes

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Custom serializer for the ShareAttributes class.
 * This handles the deserialization and serialization of the ShareAttributes data class.
 * Since Nextcloud 30, the enabled key have bee renamed to value and supports more than boolean.
 */
object ShareAttributesSerializer : KSerializer<ShareAttributes> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ShareAttributes") {
        element("scope", PrimitiveSerialDescriptor("scope", PrimitiveKind.STRING))
        element("key", PrimitiveSerialDescriptor("key", PrimitiveKind.STRING))
        element("enabled", PrimitiveSerialDescriptor("enabled", PrimitiveKind.BOOLEAN), isOptional = true)
        element("value", PrimitiveSerialDescriptor("value", PrimitiveKind.BOOLEAN), isOptional = true)
    }

    override fun deserialize(decoder: Decoder): ShareAttributes {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("Expected JsonDecoder")

        val jsonObject = input.decodeJsonElement().jsonObject
        val scope = jsonObject["scope"]?.jsonPrimitive?.content
            ?: throw SerializationException("Missing 'scope'")
        val key = jsonObject["key"]?.jsonPrimitive?.content
            ?: throw SerializationException("Missing 'key'")

        // Check for the 'enabled' or 'value' field. If neither is found, throw an exception.
        val isEnabled = jsonObject["enabled"]?.jsonPrimitive?.booleanOrNull
            ?: jsonObject["value"]?.jsonPrimitive?.booleanOrNull
            ?: throw SerializationException("Missing 'enabled' or 'value'")

        return ShareAttributes(scope, key, isEnabled)
    }

    override fun serialize(encoder: Encoder, value: ShareAttributes) {
        val output = encoder as? JsonEncoder
            ?: throw SerializationException("Expected JsonEncoder")

        val json = buildJsonObject {
            put("scope", JsonPrimitive(value.scope))
            put("key", JsonPrimitive(value.key))
            put("value", JsonPrimitive(value.isEnabled))
        }

        output.encodeJsonElement(json)
    }
}
