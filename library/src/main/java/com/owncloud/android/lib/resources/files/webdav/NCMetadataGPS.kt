/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 ZetaTom <70907959+ZetaTom@users.noreply.github.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.files.webdav

import android.util.Log
import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.PropertyFactory
import at.bitfire.dav4jvm.XmlUtils
import com.google.gson.Gson
import com.owncloud.android.lib.common.network.ExtendedProperties
import com.owncloud.android.lib.resources.files.model.GeoLocation
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class NCMetadataGPS private constructor(val geoLocation: GeoLocation?) : Property {
    class Factory : PropertyFactory {
        override fun getName() = NAME

        override fun create(parser: XmlPullParser): Property {
            try {
                val text = XmlUtils.readText(parser)
                if (!text.isNullOrEmpty()) {
                    val geoLocation = Gson().fromJson(text, GeoLocation::class.java)
                    return NCMetadataGPS(geoLocation)
                }
            } catch (e: IOException) {
                Log.e("NCMetadataGPS", "failed to create property", e)
            } catch (e: XmlPullParserException) {
                Log.e("NCMetadataGPS", "failed to create property", e)
            }
            return NCMetadataGPS(null)
        }
    }

    companion object {
        @JvmField
        val NAME = ExtendedProperties.METADATA_GPS.toPropertyName()
    }
}
