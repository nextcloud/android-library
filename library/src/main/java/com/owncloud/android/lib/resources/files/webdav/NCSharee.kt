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

import androidx.annotation.VisibleForTesting
import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.PropertyFactory
import at.bitfire.dav4jvm.XmlUtils.propertyName
import at.bitfire.dav4jvm.XmlUtils.readText
import com.owncloud.android.lib.common.network.WebdavEntry
import com.owncloud.android.lib.resources.shares.ShareType
import com.owncloud.android.lib.resources.shares.ShareeUser
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class NCSharee internal constructor(var sharees: Array<ShareeUser>) : Property {

    companion object {
        @JvmField
        val NAME =
            Property.Name(WebdavEntry.NAMESPACE_NC, WebdavEntry.EXTENDED_PROPERTY_SHAREES)
    }

    class Factory : PropertyFactory {

        override fun getName() = NAME

        override fun create(parser: XmlPullParser): NCSharee {
            // NC sharees property <nc:sharees>
            readArrayNode(parser).let { sharees ->
                return NCSharee(sharees.toTypedArray())
            }
        }

        @Throws(IOException::class, XmlPullParserException::class)
        @VisibleForTesting
        fun readArrayNode(parser: XmlPullParser): List<ShareeUser> {
            var list: List<ShareeUser> = emptyList()

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

        private fun readNCSharees(parser: XmlPullParser): List<ShareeUser> {
            val list: ArrayList<ShareeUser> = ArrayList()

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

        private fun readNCSharee(parser: XmlPullParser): ShareeUser {
            val depth = parser.depth
            var eventType = parser.eventType

            val shareeUser = ShareeUser(null, null, null)

            while (!(eventType == XmlPullParser.END_TAG && parser.depth == depth)) {
                if (eventType != XmlPullParser.TEXT) {
                    when (parser.propertyName().toString()) {
                        "http://nextcloud.org/ns:id" -> {
                            shareeUser.userId = readText(parser)
                        }
                        "http://nextcloud.org/ns:display-name" -> {
                            shareeUser.displayName = readText(parser)
                        }
                        "http://nextcloud.org/ns:type" -> {
                            shareeUser.shareType =
                                ShareType.fromValue(readText(parser)?.toInt() ?: 0)
                        }
                    }
                }

                eventType = parser.next()
            }

            return shareeUser
        }
    }
}
