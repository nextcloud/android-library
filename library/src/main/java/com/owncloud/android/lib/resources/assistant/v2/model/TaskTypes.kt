/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.v2.model

import com.owncloud.android.lib.resources.assistant.EmptyArrayAsObjectSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonObject

@Serializable
data class TaskTypes(
    val types: Map<String, TaskTypeData>
)

@Serializable
data class TaskTypeData(
    val id: String? = null,
    val name: String,
    val description: String? = null,
    @Serializable(with = ShapeMapSerializer::class)
    val inputShape: Map<String, Shape>,
    @Serializable(with = ShapeMapSerializer::class)
    val outputShape: Map<String, Shape>,
    @Serializable(with = ShapeDefaultsSerializer::class)
    val optionalInputShapeDefaults: JsonObject? = null,
    @Serializable(with = EnumValuesMapSerializer::class)
    val optionalInputShapeEnumValues: Map<String, List<EnumValue>>? = null,
    @Serializable(with = EnumValuesMapSerializer::class)
    val inputShapeEnumValues: Map<String, List<EnumValue>>? = null,
    @Serializable(with = EnumValuesMapSerializer::class)
    val outputShapeEnumValues: Map<String, List<EnumValue>>? = null,
    @Serializable(with = EnumValuesMapSerializer::class)
    val optionalOutputShapeEnumValues: Map<String, List<EnumValue>>? = null
) {
    fun isChat(): Boolean = (id == CHAT_TASK_TYPE_ID)

    fun isTranslate(): Boolean = (id == TRANSLATE_TASK_TYPE_ID)

    companion object {
        const val CHAT_TASK_TYPE_ID = "core:text2text:chat"
        const val TRANSLATE_TASK_TYPE_ID = "core:text2text:translate"
        private const val CONVERSATION_LIST_ID = "ConversationList"
        val conversationList =
            TaskTypeData(
                CONVERSATION_LIST_ID,
                "",
                "",
                mapOf(),
                mapOf()
            )
    }
}

@Serializable
data class EnumValue(
    val name: String,
    val value: String
)

@Serializable
data class Shape(
    val name: String,
    val description: String,
    val type: String
)

internal object ShapeMapSerializer :
    EmptyArrayAsObjectSerializer<Map<String, Shape>>(MapSerializer(String.serializer(), Shape.serializer()))

internal object EnumValuesMapSerializer :
    EmptyArrayAsObjectSerializer<Map<String, List<EnumValue>>>(
        MapSerializer(String.serializer(), ListSerializer(EnumValue.serializer()))
    )

internal object ShapeDefaultsSerializer :
    EmptyArrayAsObjectSerializer<JsonObject>(JsonObject.serializer())
