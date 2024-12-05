/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.extensions

import org.w3c.dom.Element

@Suppress("ReturnCount", "NestedBlockDepth")
inline fun <reified T> ArrayList<*>.processXmlData(tagName: String): T? {
    this.forEach {
        val element = it as? Element
        if (element != null && element.tagName == tagName) {
            val textContent = element.firstChild.textContent
            return when (T::class) {
                Float::class -> {
                    textContent.toFloatOrNull() as? T
                }
                Double::class -> {
                    textContent.toDoubleOrNull() as? T
                }
                else -> textContent as? T
            }
        }
    }

    return null
}
