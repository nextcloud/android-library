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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant

@Serializable
data class Conversation(
    val id: Long,
    @SerialName("user_id")
    val userId: String,
    val title: String? = null,
    val timestamp: Long,
    @SerialName("agency_conversation_token")
    val agencyConversationToken: String? = null,
    @SerialName("agency_pending_actions")
    val agencyPendingActions: JsonElement? = null
) {
    companion object {
        private const val TITLE_PRESENTATION_TIME_PATTERN = "MMMM dd, yyyy HH:mm"
    }

    @OptIn(ExperimentalTime::class)
    fun titleRepresentation(): String =
        if (title != null) {
            title
        } else {
            val instant = Instant.fromEpochSeconds(timestamp)
            val deviceZone = ZoneId.systemDefault()

            val formatter =
                DateTimeFormatter
                    .ofPattern(TITLE_PRESENTATION_TIME_PATTERN, Locale.getDefault())
                    .withZone(deviceZone)

            formatter.format(instant.toJavaInstant())
        }
}
