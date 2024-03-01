/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.status

enum class E2EVersion(val value: String) {
    V1_0("1.0"),
    V1_1("1.1"),
    V1_2("1.2"),
    V2_0("2.0"),
    UNKNOWN("");

    companion object {
        @JvmStatic
        fun fromValue(v: String): E2EVersion = E2EVersion.values().firstOrNull { it.value == v } ?: UNKNOWN
    }
}
