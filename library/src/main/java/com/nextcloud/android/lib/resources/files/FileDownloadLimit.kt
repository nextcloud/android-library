/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 ZetaTom <70907959+zetatom@users.noreply.github.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.files

import java.io.Serializable

data class FileDownloadLimit(
    val token: String,
    val limit: Int,
    val count: Int
) : Serializable
