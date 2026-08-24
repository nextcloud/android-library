/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Your Name <your@email.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.files.webdav

import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.PropertyFactory
import com.owncloud.android.lib.common.network.ExtendedProperties
import org.xmlpull.v1.XmlPullParser

class NCLockTime private constructor(val lockTime: Long) : Property {
    class Factory : PropertyFactory {
        override fun getName() = NAME

        override fun create(parser: XmlPullParser): Property {
            parser.text?.let { date ->
                return NCLockTime(date.toLong())
            }
            return NCLockTime(0)
        }
    }

    companion object {
        @JvmField
        val NAME = ExtendedProperties.LOCK_TIME.toPropertyName()
    }
}
