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
    private val chatTaskName = "Chat"
    private val translateTaskName = "Translate"

    fun isChat(): Boolean = (name == chatTaskName)

    fun isTranslate(): Boolean = (name == translateTaskName)

    companion object {
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
