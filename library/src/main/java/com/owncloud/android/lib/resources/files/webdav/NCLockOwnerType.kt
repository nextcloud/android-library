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
import com.owncloud.android.lib.resources.files.model.FileLockType
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class NCLockOwnerType private constructor(val lockOwnerType: FileLockType?) : Property {
    class Factory : PropertyFactory {
        override fun getName() = NAME

        override fun create(parser: XmlPullParser): Property {
            try {
                val text = parser.text
                if (!text.isNullOrEmpty()) {
                    return NCLockOwnerType(FileLockType.fromValue(text.toInt()))
                }
            } catch (e: IOException) {
                Log.e("NCLockOwnerType", "failed to create property", e)
            } catch (e: XmlPullParserException) {
                Log.e("NCLockOwnerType", "failed to create property", e)
            }
            return NCLockOwnerType(null)
        }
    }

    companion object {
        @JvmField
        val NAME = ExtendedProperties.LOCK_OWNER_TYPE.toPropertyName()
    }
}
