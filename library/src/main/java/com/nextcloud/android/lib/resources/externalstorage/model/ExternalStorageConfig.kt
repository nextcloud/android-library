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
 * Subset of the mount's `config` object.
 */
@Serializable
data class ExternalStorageConfig(
    // kept raw: identifiers from apps registering their own mechanisms map to Unknown instead of failing
    @SerialName("authMechanism")
    private val authMechanismIdentifier: String
) {
    val authMechanism: AuthMechanism
        get() = AuthMechanism.fromIdentifier(authMechanismIdentifier)
}
