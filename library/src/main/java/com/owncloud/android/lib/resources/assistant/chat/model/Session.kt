/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.chat.model

data class Session(
    val messageTaskId: Long?,
    val titleTaskId: Long?,
    val sessionTitle: String?,
    val sessionAgencyPendingActions: Any?
)

data class SessionTask(
    val taskId: Long
)
