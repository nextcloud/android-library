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

class NCLockOwner private constructor(val lockOwner: String?) : Property {
    class Factory : PropertyFactory {
        override fun getName() = NAME

        override fun create(parser: XmlPullParser): Property {
            try {
                val text = parser.text
                if (!text.isNullOrEmpty()) {
                    return NCLockOwner(text)
                }
            } catch (e: IOException) {
                Log.e("NCLockOwner", "failed to create property", e)
            } catch (e: XmlPullParserException) {
                Log.e("NCLockOwner", "failed to create property", e)
            }
            return NCLockOwner(null)
        }
    }

    companion object {
        @JvmField
        val NAME = ExtendedProperties.LOCK_OWNER.toPropertyName()
    }
}
