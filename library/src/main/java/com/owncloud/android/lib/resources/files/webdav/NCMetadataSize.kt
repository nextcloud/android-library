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
import at.bitfire.dav4jvm.XmlUtils
import com.google.gson.Gson
import com.owncloud.android.lib.common.network.ExtendedProperties
import com.owncloud.android.lib.resources.files.model.ImageDimension
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class NCMetadataSize private constructor(val imageDimension: ImageDimension?) : Property {
    class Factory : PropertyFactory {
        override fun getName() = NAME

        override fun create(parser: XmlPullParser): Property {
            try {
                val text = XmlUtils.readText(parser)
                if (!text.isNullOrEmpty()) {
                    val imageDimension = Gson().fromJson(text, ImageDimension::class.java)
                    return NCMetadataSize(imageDimension)
                }
            } catch (e: IOException) {
                Log.e("NCMetadataSize", "failed to create property", e)
            } catch (e: XmlPullParserException) {
                Log.e("NCMetadataSize", "failed to create property", e)
            }
            return NCMetadataSize(null)
        }
    }

    companion object {
        @JvmField
        val NAME = ExtendedProperties.METADATA_SIZE.toPropertyName()
    }
}
