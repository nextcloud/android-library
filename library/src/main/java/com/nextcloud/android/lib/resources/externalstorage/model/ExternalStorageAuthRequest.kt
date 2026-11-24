/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.externalstorage.model

import kotlinx.serialization.Serializable

@Serializable
internal data class ExternalStorageAuthRequest(
    val backendOptions: ExternalStorageCredentials
)
