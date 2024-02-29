/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023-2024 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.groupfolders

import com.google.gson.annotations.SerializedName

data class Groupfolder(
    val id: Long,
    @SerializedName("mount_point")
    val mountPoint: String
)
