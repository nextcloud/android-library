/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 ZetaTom <70907959+zetatom@users.noreply.github.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

data class Chunk(
    val id: Int,
    val start: Long,
    val length: Long
)
