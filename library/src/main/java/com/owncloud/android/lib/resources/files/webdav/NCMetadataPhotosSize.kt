/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 ZetaTom <70907959+ZetaTom@users.noreply.github.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.files.webdav

import android.util.Log
import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.PropertyFactory
import at.bitfire.dav4jvm.XmlUtils.propertyName
import at.bitfire.dav4jvm.XmlUtils.readText
import com.owncloud.android.lib.common.network.ExtendedProperties
import com.owncloud.android.lib.resources.files.model.ImageDimension
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class NCMetadataPhotosSize private constructor(val imageDimension: ImageDimension) : Property {
    class Factory : PropertyFactory {
        override fun getName() = NAME

        override fun create(parser: XmlPullParser): Property {
            return NCMetadataPhotosSize(parseText(parser))
        }

        @Suppress("NestedBlockDepth")
        private fun parseText(parser: XmlPullParser): ImageDimension {
            var width = 0.0f
            var height = 0.0f

            val depth = parser.depth
            var eventType = parser.eventType

            try {
                while (eventType != XmlPullParser.END_TAG || parser.depth != depth) {
                    if (eventType != XmlPullParser.TEXT) {
                        when (parser.propertyName().name) {
                            "width" -> readText(parser)?.let { width = it.toFloat() }
                            "height" -> readText(parser)?.let { height = it.toFloat() }
                        }
                    }

                    eventType = parser.next()
                }
            } catch (e: IOException) {
                Log.e("NCMetadataPhotosSize", "failed to create property", e)
            } catch (e: XmlPullParserException) {
                Log.e("NCMetadataPhotosSize", "failed to create property", e)
            }

            return ImageDimension(width, height)
        }
    }

    companion object {
        @JvmField
        val NAME = ExtendedProperties.METADATA_PHOTOS_SIZE.toPropertyName()
    }
}
