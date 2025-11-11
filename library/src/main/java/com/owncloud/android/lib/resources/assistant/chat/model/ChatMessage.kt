/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.resources.assistant.chat.model

import com.google.gson.annotations.SerializedName

data class ChatMessage(
    val id: Long,
    @SerializedName("session_id")
    val sessionId: Long,
    val role: String,
    val content: String,
    val timestamp: Long,
    @SerializedName("ocp_task_id")
    val ocpTaskId: Any?,
    val sources: String,
    val attachments: List<Any?>
) {
    val isHuman: Boolean = (role == "human")
}
