/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.chat.model

import com.google.gson.annotations.SerializedName

data class AgencyAction(
    val id: Long?,
    val name: String?,
    val args: Map<String, Any?>?,
    val destructive: Boolean?,
    @SerializedName("impulse_radius")
    val impulseRadius: String?,
    val icon: String?
)
