/* Nextcloud Android Library is available under MIT license
*
* @author Tobias Kaminsky
* Copyright (C) 2022 Tobias Kaminsky
* Copyright (C) 2022 Nextcloud GmbH
*
*   Permission is hereby granted, free of charge, to any person obtaining a copy
*   of this software and associated documentation files (the "Software"), to deal
*   in the Software without restriction, including without limitation the rights
*   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
*   copies of the Software, and to permit persons to whom the Software is
*   furnished to do so, subject to the following conditions:
*
*   The above copyright notice and this permission notice shall be included in
*   all copies or substantial portions of the Software.
*
*   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
*   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
*   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
*   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
*   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
*   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
*   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
*   THE SOFTWARE.
*
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
