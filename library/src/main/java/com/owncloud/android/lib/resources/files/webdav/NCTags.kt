/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Tobias Kaminsky <tobias.kaminsky@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.files.webdav

import androidx.annotation.VisibleForTesting
import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.PropertyFactory
import at.bitfire.dav4jvm.XmlUtils.propertyName
import at.bitfire.dav4jvm.XmlUtils.readText
import com.owncloud.android.lib.common.network.ExtendedProperties
import com.owncloud.android.lib.resources.tags.Tag
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class NCTags private constructor(
    val tags: Array<Tag?>
) : Property {
    class Factory : PropertyFactory {
        override fun getName() = NAME

        override fun create(parser: XmlPullParser): NCTags {
            // NC tags property <nc:system-tags>
            readArrayNode(parser).let { tags ->
                return NCTags(tags.toTypedArray())
            }
        }

        @Throws(IOException::class, XmlPullParserException::class)
        @VisibleForTesting
        fun readArrayNode(parser: XmlPullParser): List<Tag> {
            var list: List<Tag> = emptyList()

            val depth = parser.depth
            var eventType = parser.eventType
            while (!(eventType == XmlPullParser.END_TAG && parser.depth == depth)) {
                if (eventType != XmlPullParser.TEXT) {
                    list = readNCTags(parser)
                }
                if (parser.eventType == XmlPullParser.END_TAG && parser.depth == depth) {
                    return list
                }

                eventType = parser.next()
            }

            return list
        }

        private fun readNCTags(parser: XmlPullParser): List<Tag> {
            val list: ArrayList<Tag> = ArrayList()

            val depth = parser.depth
            var eventType = parser.eventType
            while (!(eventType == XmlPullParser.END_TAG && parser.depth == depth)) {
                if (eventType == XmlPullParser.START_TAG && parser.depth == depth + 1) {
                    list.add(readNCTag(parser))
                }

                eventType = parser.next()
            }

            return list
        }

        private fun readNCTag(parser: XmlPullParser): Tag {
            val depth = parser.depth
            var eventType = parser.eventType

            var id = ""
            var name = ""
            var color = ""

            while (!(eventType == XmlPullParser.END_TAG && parser.depth == depth)) {
                if (eventType != XmlPullParser.TEXT) {
                    when (parser.propertyName().toString()) {
                        "http://nextcloud.org/ns:id" -> {
                            id = readText(parser).orEmpty()
                        }

                        "http://nextcloud.org/ns:name" -> {
                            name = readText(parser).orEmpty()
                        }

                        "http://nextcloud.org/ns:color" -> {
                            color = readText(parser).orEmpty()
                        }
                    }
                }

                eventType = parser.next()
            }

            return Tag(id, name, color)
        }
    }

    companion object {
        @JvmField
        val NAME = ExtendedProperties.SYSTEM_TAGS.toPropertyName()
    }
}
