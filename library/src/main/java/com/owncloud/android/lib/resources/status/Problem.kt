/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.status

data class Problem(val type: String, val count: Int, val oldestTimestamp: Long) {
    fun toJsonWithTypeString(): String {
        return """"$type": {"count": $count, "oldest": $oldestTimestamp}"""
    }
}
