/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.resources.assistant.chat.model

import com.google.gson.annotations.SerializedName

data class Conversation(
    val id: Long,
    @SerializedName("user_id")
    val userId: String,
    val title: String,
    val timestamp: Long,
    @SerializedName("agency_conversation_token")
    val agencyConversationToken: String,
    @SerializedName("agency_pending_actions")
    val agencyPendingActions: Any?
)
