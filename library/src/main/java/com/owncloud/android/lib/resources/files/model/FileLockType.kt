/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files.model

enum class FileLockType(
    val value: Int
) {
    MANUAL(0),
    COLLABORATIVE(1),
    TOKEN(2);

    companion object {
        @JvmStatic
        fun fromValue(v: Int): FileLockType? = values().firstOrNull { it.value == v }
    }
}
