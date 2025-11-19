/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.chat.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.annotations.SerializedName
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant

data class Conversation(
    val id: Long,
    @SerializedName("user_id")
    val userId: String,
    val title: String?,
    val timestamp: Long,
    @SerializedName("agency_conversation_token")
    val agencyConversationToken: String,
    @SerializedName("agency_pending_actions")
    val agencyPendingActions: Any?
) {
    companion object {
        private const val TITLE_PRESENTATION_TIME_PATTERN = "MMMM dd, yyyy HH:mm"
    }

    @OptIn(ExperimentalTime::class)
    @RequiresApi(Build.VERSION_CODES.O)
    fun titleRepresentation(): String {
        return if (title != null) {
            title
        } else {
            val instant = Instant.fromEpochSeconds(timestamp)
            val deviceZone = ZoneId.systemDefault()

            val formatter =
                DateTimeFormatter
                    .ofPattern(TITLE_PRESENTATION_TIME_PATTERN, Locale.getDefault())
                    .withZone(deviceZone)

            return formatter.format(instant.toJavaInstant())
        }
    }
}
