/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Tobias Kaminsky <tobias.kaminsky@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.files.webdav

import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.PropertyFactory
import at.bitfire.dav4jvm.XmlUtils
import com.owncloud.android.lib.common.network.ExtendedProperties
import org.xmlpull.v1.XmlPullParser

class NCCreationTime private constructor(
    val creationTime: Long
) : Property {
    class Factory : PropertyFactory {
        override fun getName() = NAME

        override fun create(parser: XmlPullParser): Property {
            XmlUtils.readText(parser)?.let { date ->
                return NCCreationTime(date.toLong())
            }
            return NCCreationTime(0)
        }
    }

    companion object {
        @JvmField
        val NAME = ExtendedProperties.CREATION_TIME.toPropertyName()
    }
}
