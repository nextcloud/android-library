/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.chat.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ChatMessage(
    val id: Long,
    @SerialName("session_id")
    val sessionId: Long,
    val role: String,
    val content: String,
    val timestamp: Long,
    @SerialName("ocp_task_id")
    val ocpTaskId: Long? = null,
    val sources: String? = null,
    val attachments: List<JsonElement> = emptyList()
)
