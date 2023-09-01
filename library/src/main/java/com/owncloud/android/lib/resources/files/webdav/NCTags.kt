/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2022 Tobias Kaminsky
 * Copyright (C) 2022 Nextcloud GmbH
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.owncloud.android.lib.resources.files.webdav

import android.text.TextUtils
import androidx.annotation.VisibleForTesting
import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.PropertyFactory
import at.bitfire.dav4jvm.XmlUtils.propertyName
import at.bitfire.dav4jvm.XmlUtils.readText
import com.owncloud.android.lib.common.network.WebdavEntry
import com.owncloud.android.lib.resources.shares.ShareType
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class NCTags internal constructor(var tags: Array<String>) : Property {

    companion object {
        @JvmField
        val NAME =
            Property.Name(WebdavEntry.NAMESPACE_NC, WebdavEntry.EXTENDED_PROPERTY_SYSTEM_TAGS)
    }

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
}
