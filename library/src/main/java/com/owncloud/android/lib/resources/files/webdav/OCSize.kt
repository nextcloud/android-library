/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Your Name <your@email.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.files.webdav

import android.util.Log
import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.PropertyFactory
import com.owncloud.android.lib.common.network.ExtendedProperties
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class OCSize private constructor(val size: Long) : Property {
    class Factory : PropertyFactory {
        override fun getName() = NAME

        override fun create(parser: XmlPullParser): Property {
            try {
                val text = parser.text
                if (!text.isNullOrEmpty()) {
                    return OCSize(text.toLong())
                }
            } catch (e: IOException) {
                Log.e("OCSize", "failed to create property", e)
            } catch (e: XmlPullParserException) {
                Log.e("OCSize", "failed to create property", e)
            }
            return OCSize(0)
        }
    }

    companion object {
        @JvmField
        val NAME = ExtendedProperties.NAME_SIZE.toPropertyName()
    }
}
