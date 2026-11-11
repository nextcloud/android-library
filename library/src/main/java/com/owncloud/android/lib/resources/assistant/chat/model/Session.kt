/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.chat.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Session(
    val messageTaskId: Long? = null,
    val titleTaskId: Long? = null,
    val sessionTitle: String? = null,
    val sessionAgencyPendingActions: JsonElement? = null
)

@Serializable
data class SessionTask(
    val taskId: Long
)
