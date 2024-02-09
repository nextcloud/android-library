package com.nextcloud.extensions

import org.w3c.dom.Element

inline fun <reified T> ArrayList<*>.processXmlData(tagName: String): T? {
    this.forEach {
        val element = it as? Element
        if (element != null && element.tagName == tagName) {
            val textContent = element.firstChild.textContent

            return when (T::class) {
                Float::class -> textContent.toDouble() as T
                Double::class -> textContent.toDouble() as T
                else -> textContent as T
            }
        }
    }

    return null
}
