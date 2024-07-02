/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.files.webdav

import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.PropertyFactory
import at.bitfire.dav4jvm.XmlUtils
import at.bitfire.dav4jvm.property.webdav.NS_WEBDAV
import com.owncloud.android.lib.common.network.WebdavUtils
import org.xmlpull.v1.XmlPullParser

class NCGetLastModified private constructor(val lastModified: Long) : Property {
    class Factory : PropertyFactory {
        override fun getName() = NAME

        override fun create(parser: XmlPullParser): NCGetLastModified {
            // <!ELEMENT getlastmodified (#PCDATA) >
            XmlUtils.readText(parser)?.let { rawDate ->
                val date = WebdavUtils.parseResponseDate(rawDate)
                if (date != null) {
                    return NCGetLastModified(date.time)
                }
            }
            return NCGetLastModified(0)
        }
    }

    companion object {
        @JvmField
        val NAME = Property.Name(NS_WEBDAV, "getlastmodified")
    }
}
