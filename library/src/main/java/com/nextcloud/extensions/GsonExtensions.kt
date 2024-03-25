/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Alper Ozturk <alper_ozturk@proton.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.extensions

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.apache.jackrabbit.webdav.property.DavProperty

inline fun <reified T> Gson.fromDavProperty(davProperty: DavProperty<*>?): T? {
    return if (davProperty != null && davProperty.value != null) {
        try {
            fromJson(davProperty.value.toString(), T::class.java)
        } catch (e: JsonSyntaxException) {
            null
        }
    } else {
        null
    }
}
