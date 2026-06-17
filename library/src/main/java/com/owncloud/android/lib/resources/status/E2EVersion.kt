/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.status

enum class E2EVersion(
    val values: List<String>
) {
    V1_0(listOf("1", "1.0")),
    V1_1(listOf("1.1")),
    V1_2(listOf("1.2")),
    V2_0(listOf("2", "2.0")),
    V2_1(listOf("2.1")),
    UNKNOWN(listOf(""));

    val value: String = values.last()

    var unknownValue: String? = ""

    companion object {
        fun max(): E2EVersion = V2_1

        @JvmStatic
        fun fromValue(v: String?): E2EVersion =
            entries.find { v in it.values }
                ?: UNKNOWN.also { it.unknownValue = v }
    }
}
