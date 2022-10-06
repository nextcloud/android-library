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

import android.text.TextUtils
import android.util.Log
import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.PropertyFactory
import at.bitfire.dav4jvm.XmlUtils.readText
import com.owncloud.android.lib.common.network.WebdavEntry
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class NCFavorite internal constructor(var isOcFavorite: Boolean) : Property {

    class Factory : PropertyFactory {
        override fun create(parser: XmlPullParser): Property {
            try {
                val text = readText(parser)
                if (!TextUtils.isEmpty(text)) {
                    return NCFavorite("1" == text)
                }
            } catch (e: IOException) {
                Log.e("OCFavorite", "failed to create property", e)
            } catch (e: XmlPullParserException) {
                Log.e("OCFavorite", "failed to create property", e)
            }
            return NCFavorite(false)
        }

        override fun getName(): Property.Name {
            return NAME
        }
    }

    companion object {
        @JvmField
        val NAME: Property.Name =
            Property.Name(WebdavEntry.NAMESPACE_OC, WebdavEntry.EXTENDED_PROPERTY_FAVORITE)
    }
}
