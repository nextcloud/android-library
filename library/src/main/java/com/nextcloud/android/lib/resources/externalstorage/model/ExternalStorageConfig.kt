/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.externalstorage.model

import kotlinx.serialization.Serializable

/**
 * Subset of the mount's `config` object. [authMechanism] is the auth identifier (e.g.
 * `password::password`); [userProvided] is only sent by the server when the user must supply
 * credentials themselves, hence the `false` default.
 */
@Serializable
data class ExternalStorageConfig(
    val authMechanism: String,
    val userProvided: Boolean = false
)
