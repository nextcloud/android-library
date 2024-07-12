/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.files.webdav

import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.PropertyFactory
import at.bitfire.dav4jvm.XmlUtils
import com.owncloud.android.lib.common.network.ExtendedProperties
import com.owncloud.android.lib.common.network.WebdavEntry
import org.xmlpull.v1.XmlPullParser

class NCMountType private constructor(val mountType: WebdavEntry.MountType) : Property {
    class Factory : PropertyFactory {
        override fun getName() = NAME

        override fun create(parser: XmlPullParser): NCMountType {
            // <!ELEMENT <nc:mount-type> (#PCDATA) >
            val type = XmlUtils.readText(parser)
            return NCMountType(
                when (type) {
                    "external" -> WebdavEntry.MountType.EXTERNAL
                    "group" -> WebdavEntry.MountType.GROUP
                    else -> WebdavEntry.MountType.INTERNAL
                }
            )
        }
    }

    companion object {
        @JvmField
        val NAME = ExtendedProperties.MOUNT_TYPE.toPropertyName()
    }
}
