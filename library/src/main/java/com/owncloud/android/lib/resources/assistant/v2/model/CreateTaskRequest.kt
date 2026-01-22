/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant.v2.model

import kotlinx.serialization.Serializable

@Serializable
data class InputField(
    val input: String
)

@Serializable
data class CreateTaskRequest(
    val input: InputField,
    val type: String?,
    val appId: String = "assistant",
    val customId: String = ""
)
