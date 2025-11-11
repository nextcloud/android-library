/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.resources.assistant.chat.model

import com.google.gson.annotations.SerializedName
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant

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
    companion object {
        private const val TIMESTAMP_PRESENTATION_TIME_PATTERN = "HH:mm"
    }

    fun isHuman(): Boolean {
        return role == "human"
    }

    @OptIn(ExperimentalTime::class)
    fun timestampRepresentation(): String {
        val instant = Instant.fromEpochSeconds(timestamp)
        val deviceZone = ZoneId.systemDefault()

        val formatter = DateTimeFormatter.ofPattern(TIMESTAMP_PRESENTATION_TIME_PATTERN, Locale.getDefault())
            .withZone(deviceZone)

        return formatter.format(instant.toJavaInstant())
    }
}
