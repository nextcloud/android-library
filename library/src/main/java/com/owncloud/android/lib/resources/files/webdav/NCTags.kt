/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.files.webdav

import android.text.TextUtils
import androidx.annotation.VisibleForTesting
import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.PropertyFactory
import at.bitfire.dav4jvm.XmlUtils.propertyName
import at.bitfire.dav4jvm.XmlUtils.readText
import com.owncloud.android.lib.common.network.ExtendedProperties
import com.owncloud.android.lib.resources.shares.ShareType
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class NCTags private constructor(val tags: Array<String>) : Property {
    class Factory : PropertyFactory {
        override fun getName() = NAME

        override fun create(parser: XmlPullParser): NCTags {
            // NC sharees property <nc:system-tags>
            readArrayNode(parser).let { sharees ->
                return NCTags(sharees.toTypedArray())
            }
        }

        @Throws(IOException::class, XmlPullParserException::class)
        @VisibleForTesting
        fun readArrayNode(parser: XmlPullParser): List<String> {
            var list: List<String> = emptyList()

            val depth = parser.depth
            var eventType = parser.eventType
            while (!(eventType == XmlPullParser.END_TAG && parser.depth == depth)) {
                if (eventType != XmlPullParser.TEXT) {
                    list = readNCSharees(parser)
                }
                if (parser.eventType == XmlPullParser.END_TAG && parser.depth == depth) {
                    return list
                }

                eventType = parser.next()
            }

            return list
        }

        private fun readNCSharees(parser: XmlPullParser): List<String> {
            val list: ArrayList<String> = ArrayList()

            val depth = parser.depth
            var eventType = parser.eventType
            while (!(eventType == XmlPullParser.END_TAG && parser.depth == depth)) {
                if (eventType == XmlPullParser.START_TAG && parser.depth == depth + 1) {
                    list.add(readNCSharee(parser))
                }

                eventType = parser.next()
            }

            return list
        }

        private fun readNCSharee(parser: XmlPullParser): String {
            val depth = parser.depth
            var eventType = parser.eventType

            var userId: String? = null
            var displayName: String? = null
            var shareType: ShareType? = null

            while (!(eventType == XmlPullParser.END_TAG && parser.depth == depth)) {
                if (eventType != XmlPullParser.TEXT) {
                    when (parser.propertyName().toString()) {
                        "http://nextcloud.org/ns:id" -> {
                            userId = readText(parser)
                        }

                        "http://nextcloud.org/ns:display-name" -> {
                            displayName = readText(parser)
                        }

                        "http://nextcloud.org/ns:type" -> {
                            shareType =
                                ShareType.fromValue(readText(parser)?.toInt() ?: 0)
                        }
                    }
                }

                if (!TextUtils.isEmpty(parser.text)) {
                    return parser.text
                }
                eventType = parser.next()
            }

            return ""
        }
    }

    companion object {
        @JvmField
        val NAME = ExtendedProperties.SYSTEM_TAGS.toPropertyName()
    }
}
