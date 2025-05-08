/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.extensions

import com.google.gson.JsonObject

@Suppress("ReturnCount")
fun JsonObject?.getBoolean(key: String): Boolean? {
    if (this == null) {
        return null
    }

    if (has(key) && get(key).isJsonPrimitive) {
        return get(key).asBoolean
    }

    return null
}
