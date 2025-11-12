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
    val outputShape: Map<String, Shape>
) {
    private val chatTaskName = "Chat"

    fun isChat(): Boolean = (name == chatTaskName)

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

data class Shape(
    val name: String,
    val description: String,
    val type: String
)
