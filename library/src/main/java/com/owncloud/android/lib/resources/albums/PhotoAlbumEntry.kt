/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 TSI-mc <surinder.kumar@t-systems.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.resources.albums

import com.owncloud.android.lib.common.network.WebdavEntry
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.MultiStatusResponse
import org.apache.jackrabbit.webdav.property.DavPropertyName
import org.apache.jackrabbit.webdav.property.DavPropertySet
import org.apache.jackrabbit.webdav.xml.Namespace
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhotoAlbumEntry(response: MultiStatusResponse) {
    val href: String
    val lastPhoto: Long
    val nbItems: Int
    val location: String?
    private val dateRange: String?

    companion object {
        val dateFormat = SimpleDateFormat("MMM yyyy", Locale.US)
    }

    init {

        href = response.href

        val properties = response.getProperties(HttpStatus.SC_OK)

        this.lastPhoto = parseLong(parseString(properties, WebdavEntry.PROPERTY_LAST_PHOTO))
        this.nbItems = parseInt(parseString(properties, WebdavEntry.PROPERTY_NB_ITEMS))
        this.location = parseString(properties, WebdavEntry.PROPERTY_LOCATION)
        this.dateRange = parseString(properties, WebdavEntry.PROPERTY_DATE_RANGE)
    }

    private fun parseString(props: DavPropertySet, name: String): String? {
        val propName = DavPropertyName.create(name, Namespace.getNamespace("nc", WebdavEntry.NAMESPACE_NC))
        val prop = props[propName]
        return if (prop != null && prop.value != null) prop.value.toString() else null
    }

    private fun parseInt(value: String?): Int {
        return try {
            value?.toInt() ?: 0
        } catch (e: NumberFormatException) {
            0
        }
    }

    private fun parseLong(value: String?): Long {
        return try {
            value?.toLong() ?: 0L
        } catch (e: NumberFormatException) {
            0L
        }
    }

    val albumName: String
        get() {
            var href = href
            if (href.isEmpty()) {
                return ""
            }

            // Remove trailing slash if present
            if (href.endsWith("/")) {
                href = href.substring(0, href.length - 1)
            }

            // Split and return last part
            val parts = href.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return if (parts.isNotEmpty()) parts[parts.size - 1] else ""
        }

    val createdDate: String
        get() {
            val jsonRange = dateRange
            val currentDate = Date(System.currentTimeMillis())
            if (jsonRange.isNullOrEmpty()) {
                return dateFormat.format(currentDate)
            }

            try {
                val obj = JSONObject(jsonRange)
                val startTimestamp = obj.optLong("start", 0)

                if (startTimestamp > 0) {
                    val date = Date(startTimestamp * 1000L) // Convert to milliseconds
                    return dateFormat.format(date)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                return dateFormat.format(currentDate)
            }
            return dateFormat.format(currentDate)
        }
}