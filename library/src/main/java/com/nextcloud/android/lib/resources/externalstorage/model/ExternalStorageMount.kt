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
 * A mount returned by `files_external`: [name] is the mount folder, [path] its parent folder
 * relative to the user's root (empty when mounted at the root), [backend] the human-readable backend
 * name and [backendClass] its identifier (e.g. `smb`, `sftp`). [config] is null on servers that
 * don't send it.
 */
@Serializable
data class ExternalStorageMount(
    val id: Long,
    val name: String,
    val path: String,
    val type: String,
    val backend: String,
    @SerialName("class")
    val backendClass: String,
    val scope: MountScope,
    val permissions: Int,
    val config: ExternalStorageConfig? = null
)
