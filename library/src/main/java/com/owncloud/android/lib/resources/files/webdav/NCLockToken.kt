/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Tobias Kaminsky <tobias.kaminsky@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.files.webdav

import android.util.Log
import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.PropertyFactory
import at.bitfire.dav4jvm.XmlUtils.readText
import com.owncloud.android.lib.common.network.ExtendedProperties
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class NCLockToken private constructor(
    val lockToken: String?
) : Property {
    class Factory : PropertyFactory {
        override fun getName() = NAME

        override fun create(parser: XmlPullParser): Property {
            try {
                val text = readText(parser)
                if (!text.isNullOrEmpty()) {
                    return NCLockToken(text)
                }
            } catch (e: IOException) {
                Log.e("NCLockToken", "failed to create property", e)
            } catch (e: XmlPullParserException) {
                Log.e("NCLockToken", "failed to create property", e)
            }
            return NCLockToken(null)
        }
    }

    companion object {
        @JvmField
        val NAME = ExtendedProperties.LOCK_TOKEN.toPropertyName()
    }
}
