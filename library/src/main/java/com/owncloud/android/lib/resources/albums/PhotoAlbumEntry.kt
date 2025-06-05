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
        private val dateFormat = SimpleDateFormat("MMM yyyy", Locale.US)
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
        } catch (_: NumberFormatException) {
            0
        }
    }

    private fun parseLong(value: String?): Long {
        return try {
            value?.toLong() ?: 0L
        } catch (_: NumberFormatException) {
            0L
        }
    }

    val albumName: String
        get() {
            return href
                .removeSuffix("/")
                .substringAfterLast("/")
                .takeIf { it.isNotEmpty() } ?: ""
        }

    val createdDate: String
        get() {
            val currentDate = Date(System.currentTimeMillis())

            return try {
                val obj = JSONObject(dateRange ?: return dateFormat.format(currentDate))
                val startTimestamp = obj.optLong("start", 0)
                if (startTimestamp > 0)
                    dateFormat.format(Date(startTimestamp * 1000L))
                else
                    dateFormat.format(currentDate)
            } catch (e: JSONException) {
                e.printStackTrace()
                dateFormat.format(currentDate)
            }
        }
}