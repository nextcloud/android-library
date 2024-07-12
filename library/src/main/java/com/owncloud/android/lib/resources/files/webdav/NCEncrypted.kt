/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.files.webdav

import android.util.Log
import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.PropertyFactory
import at.bitfire.dav4jvm.XmlUtils
import com.owncloud.android.lib.common.network.ExtendedProperties
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class NCEncrypted private constructor(val encrypted: Boolean) : Property {
    class Factory : PropertyFactory {
        override fun getName() = NAME

        override fun create(parser: XmlPullParser): Property {
            try {
                val text = XmlUtils.readText(parser)
                if (!text.isNullOrEmpty()) {
                    return NCEncrypted("1" == text)
                }
            } catch (e: IOException) {
                Log.e("NCEncrypted", "failed to create property", e)
            } catch (e: XmlPullParserException) {
                Log.e("NCEncrypted", "failed to create property", e)
            }
            return NCEncrypted(false)
        }
    }

    companion object {
        @JvmField
        val NAME = ExtendedProperties.IS_ENCRYPTED.toPropertyName()
    }
}
