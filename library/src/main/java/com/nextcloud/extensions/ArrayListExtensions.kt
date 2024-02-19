/*
 * Nextcloud Android client application
 *
 * @author Alper Ozturk
 * Copyright (C) 2024 Alper Ozturk
 * Copyright (C) 2024 Nextcloud GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
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
                    val floatValue = textContent.toFloatOrNull()
                    if (floatValue != null) floatValue as T else null
                }
                Double::class -> {
                    val doubleValue = textContent.toDoubleOrNull()
                    if (doubleValue != null) doubleValue as T else null
                }
                else -> return null
            }
        }
    }

    return null
}
