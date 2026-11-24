/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.externalstorage.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Server's auth response.
 * [backend] is the external storage type (e.g. smb)
 * [status] it the external storage status (e.g. Success, Unauthorized...)
 */
@Serializable
data class ExternalStorageAuthResponse(
    val id: Long,
    val mountPoint: String,
    val backend: String,
    @SerialName("authMechanism")
    private val authMechanismIdentifier: String,
    val userProvided: Boolean,
    val type: MountScope,
    // the server sends a bare integer, which an enum can't be decoded from
    @SerialName("status")
    private val statusCode: Int? = null,
    val statusMessage: String? = null
) {
    val authMechanism: AuthMechanism
        get() = AuthMechanism.fromIdentifier(authMechanismIdentifier)

    val status: StorageStatus?
        get() = statusCode?.let(StorageStatus::fromCode)
}
