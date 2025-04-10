/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.nextcloud.extensions

import com.google.gson.JsonObject

fun JsonObject?.getBoolean(key: String): Boolean? {
    if (this == null) {
        return null
    }

    if (has(key) && get(key).isJsonPrimitive) {
        return get(key).asBoolean
    }

    return null
}
