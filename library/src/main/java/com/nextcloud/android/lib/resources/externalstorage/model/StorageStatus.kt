/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.externalstorage.model

@Suppress("MagicNumber")
enum class StorageStatus(
    val code: Int
) {
    Success(0),
    Error(1),
    Indeterminate(2),
    IncompleteConfiguration(3),
    Unauthorized(4),
    Timeout(5),
    NetworkError(6),
    Unknown(-1);

    companion object {
        fun fromCode(code: Int): StorageStatus = entries.firstOrNull { it != Unknown && it.code == code } ?: Unknown
    }
}
