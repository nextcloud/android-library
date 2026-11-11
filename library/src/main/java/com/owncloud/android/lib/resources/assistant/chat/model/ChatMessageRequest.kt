/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.chat.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageRequest(
    val sessionId: String,
    val role: String,
    val content: String,
    val timestamp: Long,
    val attachments: List<String>? = null,
    val firstHumanMessage: Boolean = false
)
