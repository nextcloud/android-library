/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Your Name <your@email.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
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
            val cls: Class<out Element> =
                when (typeName) {
                    "Button" ->
                        Class.forName("com.nextcloud.android.lib.resources.clientintegration.Button")
                            as Class<out Element>

                    "Text" ->
                        Class.forName("com.nextcloud.android.lib.resources.clientintegration.Text")
                            as Class<out Element>

                    "Image" ->
                        Class.forName("com.nextcloud.android.lib.resources.clientintegration.Image")
                            as Class<out Element>

                    "URL" ->
                        Class.forName("com.nextcloud.android.lib.resources.clientintegration.URL")
                            as Class<out Element>

                    else -> return null
                }

            return Gson().fromJson(json, cls)
        } catch (e: ClassNotFoundException) {
            throw JsonParseException(e)
        }
    }
}
