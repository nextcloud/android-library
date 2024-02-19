package com.nextcloud.extensions

import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

fun createElement(
    xml: String,
    value: String
): Element {
    val builder =
        DocumentBuilderFactory.newInstance().run {
            newDocumentBuilder()
        }
    val document = builder.newDocument()
    val element =
        document.createElement(xml).apply {
            textContent = value
        }
    document.appendChild(element)
    return element
}
