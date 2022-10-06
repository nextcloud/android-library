/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2022 Tobias Kaminsky
 * Copyright (C) 2022 Nextcloud GmbH
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.owncloud.android.lib.resources.files.webdav

import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.PropertyFactory
import at.bitfire.dav4jvm.XmlUtils
import com.owncloud.android.lib.common.network.WebdavEntry
import org.xmlpull.v1.XmlPullParser

class NCMountType internal constructor(var type: WebdavEntry.MountType) : Property {

    companion object {
        @JvmField
        val NAME = Property.Name(WebdavEntry.NAMESPACE_NC, WebdavEntry.EXTENDED_PROPERTY_MOUNT_TYPE)
    }

    class Factory : PropertyFactory {

        override fun getName() = NAME

        override fun create(parser: XmlPullParser): NCMountType {
            // <!ELEMENT <nc:mount-type> (#PCDATA) >
            val r = XmlUtils.readText(parser)?.let { type ->
                when (type) {
                    "external" -> WebdavEntry.MountType.EXTERNAL
                    "group" -> WebdavEntry.MountType.GROUP
                    else -> WebdavEntry.MountType.INTERNAL
                }
            } ?: WebdavEntry.MountType.INTERNAL
            return NCMountType(r)
        }
    }
}
