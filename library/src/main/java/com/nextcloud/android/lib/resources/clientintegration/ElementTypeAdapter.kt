/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2025 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.clientintegration

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class ElementTypeAdapter :
    JsonSerializer<Element>,
    JsonDeserializer<Element> {
    override fun serialize(
        src: Element,
        type: Type,
        context: JsonSerializationContext
    ): JsonElement {
        // needs to be a new Gson instance, otherwise we end up in a loop
        val element = Gson().toJsonTree(src)
        element.asJsonObject.addProperty("element", src.javaClass.name)

        return element
    }

    @Throws(JsonParseException::class, ClassNotFoundException::class, Throwable::class)
    override fun deserialize(
        json: JsonElement,
        type: Type,
        context: JsonDeserializationContext
    ): Element? {
        val jsonObject = json.asJsonObject
        val typeName = jsonObject.get("element").asString

        try {
            val prefix = "com.nextcloud.android.lib.resources.clientintegration"
            val cls: Class<out Element> =
                when (typeName) {
                    "Button" -> {
                        Class.forName("$prefix.LayoutButton") as Class<out Element>
                    }

                    "Text" -> {
                        Class.forName("$prefix.LayoutText") as Class<out Element>
                    }

                    "Image" -> {
                        Class.forName("$prefix.LayoutImage") as Class<out Element>
                    }

                    "URL" -> {
                        Class.forName("$prefix.LayoutURL") as Class<out Element>
                    }

                    else -> {
                        return null
                    }
                }

            return Gson().fromJson(json, cls)
        } catch (e: ClassNotFoundException) {
            throw JsonParseException(e)
        }
    }
}
