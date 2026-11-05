/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.v2.model

data class TaskTypes(
    val types: Map<String, TaskTypeData>
)

data class TaskTypeData(
    val id: String?,
    val name: String,
    val description: String?,
    val inputShape: Map<String, Shape>,
    val outputShape: Map<String, Shape>,
    val optionalInputShapeDefaults: Map<String, Any>? = null,
    val optionalInputShapeEnumValues: Map<String, List<EnumValue>>? = null,
    val inputShapeEnumValues: Map<String, List<EnumValue>>? = null,
    val outputShapeEnumValues: Map<String, List<EnumValue>>? = null,
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

data class EnumValue(
    val name: String,
    val value: String
)

data class Shape(
    val name: String,
    val description: String,
    val type: String
)
