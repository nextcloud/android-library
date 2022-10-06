/*
 * Nextcloud Talk application
 *
 * @author Mario Danic
 * @author Andy Scherzinger
 * Copyright (C) 2021 Andy Scherzinger <info@andy-scherzinger.de>
 * Copyright (C) 2017-2019 Mario Danic <mario@lovelyhq.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextcloud.talk.components.filebrowser.models.properties

import android.text.TextUtils
import android.util.Log
import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.PropertyFactory
import at.bitfire.dav4jvm.XmlUtils.readText
import com.owncloud.android.lib.common.network.WebdavEntry
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class OCOwnerDisplayName private constructor(var string: String?) : Property {

    class Factory : PropertyFactory {
        override fun create(parser: XmlPullParser): Property {
            try {
                val text = readText(parser)
                if (!TextUtils.isEmpty(text)) {
                    return OCOwnerDisplayName(text)
                }
            } catch (e: IOException) {
                Log.e("OCOwnerDisplayName", "failed to create property", e)
            } catch (e: XmlPullParserException) {
                Log.e("OCOwnerDisplayName", "failed to create property", e)
            }
            return OCOwnerDisplayName("")
        }

        override fun getName(): Property.Name {
            return NAME
        }
    }

    companion object {
        @JvmField
        val NAME: Property.Name = Property.Name(
            WebdavEntry.NAMESPACE_OC,
            WebdavEntry.EXTENDED_PROPERTY_OWNER_DISPLAY_NAME
        )
    }
}
