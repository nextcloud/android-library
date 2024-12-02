/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
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
                    // only add non-null users
                    readNCSharee(parser)?.let { list.add(it) }
                }

                eventType = parser.next()
            }

            return list
        }

        private fun readNCSharee(parser: XmlPullParser): ShareeUser? {
            val depth = parser.depth
            var eventType = parser.eventType

            var userId: String? = null
            var displayName: String? = null
            var shareType: ShareType? = null

            while (eventType != XmlPullParser.END_TAG || parser.depth != depth) {
                if (eventType != XmlPullParser.TEXT) {
                    when (parser.propertyName()) {
                        ExtendedProperties.SHAREES_ID.toPropertyName() -> {
                            userId = readText(parser)
                        }

                        ExtendedProperties.SHAREES_DISPLAY_NAME.toPropertyName() -> {
                            displayName = readText(parser)
                        }

                        ExtendedProperties.SHAREES_SHARE_TYPE.toPropertyName() -> {
                            shareType = ShareType.fromValue(readText(parser)?.toInt() ?: 0)
                        }
                    }
                }

                eventType = parser.next()
            }

            // check that user is an actual sharee - e.g. exclude link shares

            val isSupportedShareType =
                ShareType.EMAIL == shareType ||
                    ShareType.FEDERATED == shareType ||
                    ShareType.GROUP == shareType ||
                    ShareType.ROOM == shareType

            return if (userId.isNullOrEmpty() || (displayName.isNullOrEmpty() && !isSupportedShareType)) {
                null
            } else {
                ShareeUser(userId, displayName, shareType)
            }
        }
    }

    companion object {
        @JvmField
        val NAME = ExtendedProperties.SHAREES.toPropertyName()
    }
}
