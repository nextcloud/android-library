/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.externalstorage.model

import kotlinx.serialization.Serializable

/**
 * External storage credentials
 */
@Serializable
data class ExternalStorageCredentials(
    val user: String,
    val password: String
)
