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
import at.bitfire.dav4jvm.property.webdav.NS_WEBDAV
import com.owncloud.android.lib.common.network.WebdavUtils
import org.xmlpull.v1.XmlPullParser

class NCEtag private constructor(val etag: String?) : Property {
    class Factory : PropertyFactory {
        override fun getName() = NAME

        override fun create(parser: XmlPullParser): NCEtag {
            // <!ELEMENT getetag (#PCDATA) >
            XmlUtils.readText(parser)?.let { rawEtag ->
                return NCEtag(WebdavUtils.parseEtag(rawEtag))
            }
            return NCEtag(null)
        }
    }

    companion object {
        @JvmField
        val NAME = Property.Name(NS_WEBDAV, "getetag")
    }
}
