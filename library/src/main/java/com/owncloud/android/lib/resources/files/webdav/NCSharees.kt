/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2022 Tobias Kaminsky
 *   Copyright (C) 2022 Nextcloud GmbH
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 */

package com.owncloud.android.lib.resources.files.webdav

import androidx.annotation.VisibleForTesting
import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.PropertyFactory
import at.bitfire.dav4jvm.XmlUtils.propertyName
import at.bitfire.dav4jvm.XmlUtils.readText
import com.owncloud.android.lib.common.network.ExtendedProperties
import com.owncloud.android.lib.resources.shares.ShareType
import com.owncloud.android.lib.resources.shares.ShareeUser
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class NCSharees private constructor(val sharees: Array<ShareeUser>) : Property {
    class Factory : PropertyFactory {
        override fun getName() = NAME

        override fun create(parser: XmlPullParser): NCSharees {
            // NC sharees property <nc:sharees>
            readArrayNode(parser).let { sharees ->
                return NCSharees(sharees.toTypedArray())
            }
        }

        @Throws(IOException::class, XmlPullParserException::class)
        @VisibleForTesting
        fun readArrayNode(parser: XmlPullParser): List<ShareeUser> {
            var list: List<ShareeUser> = emptyList()

            val depth = parser.depth
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_TAG || parser.depth != depth) {
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
            while (eventType != XmlPullParser.END_TAG || parser.depth != depth) {
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

            var userId: String? = null
            var displayName: String? = null
            var shareType: ShareType? = null

            while (eventType != XmlPullParser.END_TAG || parser.depth != depth) {
                if (eventType != XmlPullParser.TEXT) {
                    when (parser.propertyName().name) {
                        ExtendedProperties.SHAREES_ID.name -> {
                            userId = readText(parser)
                        }

                        ExtendedProperties.SHAREES_DISPLAY_NAME.name -> {
                            displayName = readText(parser)
                        }

                        ExtendedProperties.SHAREES_SHARE_TYPE.name -> {
                            shareType = ShareType.fromValue(readText(parser)?.toInt() ?: 0)
                        }
                    }
                }

                eventType = parser.next()
            }

            return ShareeUser(userId, displayName, shareType)
        }
    }

    companion object {
        @JvmField
        val NAME = ExtendedProperties.SHAREES.toPropertyName()
    }
}
