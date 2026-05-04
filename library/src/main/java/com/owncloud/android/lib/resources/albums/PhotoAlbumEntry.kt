/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 TSI-mc <surinder.kumar@t-systems.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.albums

import com.owncloud.android.lib.common.network.WebdavEntry
import com.owncloud.android.lib.resources.shares.ShareType
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.MultiStatusResponse
import org.apache.jackrabbit.webdav.property.DavPropertyName
import org.apache.jackrabbit.webdav.property.DavPropertySet
import org.apache.jackrabbit.webdav.xml.Namespace
import org.json.JSONException
import org.json.JSONObject
import org.w3c.dom.Element
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class PhotoAlbumEntry(
    // required for providing album share link url
    baseUri: String,
    response: MultiStatusResponse
) {
    val href: String
    val lastPhoto: Long
    val nbItems: Int
    val location: String?
    private val dateRange: String?
    var collaborators = arrayOf<Collaborators>()
        private set

    private var shareBaseUri: String? = null

    companion object {
        private const val MILLIS = 1000L
    }

    init {
        // will be used to provide full share link for album
        shareBaseUri = "$baseUri/apps/photos/public/"

        href = response.href

        val properties = response.getProperties(HttpStatus.SC_OK)

        this.lastPhoto = parseLong(parseString(properties, WebdavEntry.PROPERTY_LAST_PHOTO))
        this.nbItems = parseInt(parseString(properties, WebdavEntry.PROPERTY_NB_ITEMS))
        this.location = parseString(properties, WebdavEntry.PROPERTY_LOCATION)
        this.dateRange = parseString(properties, WebdavEntry.PROPERTY_DATE_RANGE)
        parseCollaborators(properties)
    }

    private fun parseString(
        props: DavPropertySet,
        name: String
    ): String? {
        val propName = DavPropertyName.create(name, Namespace.getNamespace("nc", WebdavEntry.NAMESPACE_NC))
        val prop = props[propName]
        return if (prop != null && prop.value != null) prop.value.toString() else null
    }

    private fun parseInt(value: String?): Int =
        try {
            value?.toInt() ?: 0
        } catch (_: NumberFormatException) {
            0
        }

    private fun parseLong(value: String?): Long =
        try {
            value?.toLong() ?: 0L
        } catch (_: NumberFormatException) {
            0L
        }

    private fun parseCollaborators(properties: DavPropertySet) {
        val collaboratorsProp =
            properties[WebdavEntry.PROPERTY_COLLABORATORS, Namespace.getNamespace("nc", WebdavEntry.NAMESPACE_NC)]
        if (collaboratorsProp != null && collaboratorsProp.value != null) {
            if (collaboratorsProp.value is ArrayList<*>) {
                val list = collaboratorsProp.value as ArrayList<*>
                val tempList: MutableList<Collaborators> = ArrayList()
                for (i in list.indices) {
                    val element = list[i] as Element
                    val collaborator = createCollaborators(element)
                    tempList.add(collaborator)
                }
                collaborators = tempList.toTypedArray()
            } else {
                // single item or empty
                val element = collaboratorsProp.value as Element
                val collaborator = createCollaborators(element)
                collaborators = arrayOf(collaborator)
            }
        }
    }

    private fun createCollaborators(element: Element): Collaborators {
        val id = extractId(element)
        val label = extractLabel(element)
        val shareType = extractShareType(element)
        return Collaborators(id, label, shareType, "$shareBaseUri$id")
    }

    private fun extractLabel(element: Element): String {
        val displayName = element.getElementsByTagName(WebdavEntry.COLLABORATORS_SHARE_LABEL).item(0)
        return if (displayName != null && displayName.firstChild != null) {
            displayName.firstChild.nodeValue
        } else {
            ""
        }
    }

    private fun extractId(element: Element): String {
        val userId = element.getElementsByTagName(WebdavEntry.SHAREES_ID).item(0)
        return if (userId != null && userId.firstChild != null) {
            userId.firstChild.nodeValue
        } else {
            ""
        }
    }

    private fun extractShareType(element: Element): ShareType {
        val shareType = element.getElementsByTagName(WebdavEntry.SHAREES_SHARE_TYPE).item(0)
        if (shareType != null && shareType.firstChild != null) {
            val value = shareType.firstChild.nodeValue.toInt()
            return ShareType.fromValue(value)
        }
        return ShareType.NO_SHARED
    }

    val albumName: String
        get() {
            // use decoder to show correct path
            return URLDecoder.decode(
                href
                    .removeSuffix("/")
                    .substringAfterLast("/")
                    .takeIf { it.isNotEmpty() } ?: "", StandardCharsets.UTF_8.name())
        }

    val createdDate: Long
        get() {
            val defaultTimeStamp = System.currentTimeMillis()
            return try {
                val obj = JSONObject(dateRange ?: return defaultTimeStamp)
                val startTimestamp = obj.optLong("start", 0)
                if (startTimestamp > 0) {
                    startTimestamp * MILLIS
                } else {
                    defaultTimeStamp
                }
            } catch (_: JSONException) {
                defaultTimeStamp
            }
        }
}

data class Collaborators(val id: String?, val label: String?, val type: ShareType?, val shareLink: String?)
