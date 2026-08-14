/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
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
import org.json.JSONObject
import org.w3c.dom.Element
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class PhotoAlbumEntry(
    baseUri: String,
    response: MultiStatusResponse
) {
    val href: String = response.href
    val lastPhoto: Long
    val nbItems: Int
    val location: String?
    val collaborators: List<Collaborator>

    private val dateRange: String?

    init {
        val properties = response.getProperties(HttpStatus.SC_OK)

        lastPhoto = properties.ncString(WebdavEntry.PROPERTY_LAST_PHOTO)?.toLongOrNull() ?: 0L
        nbItems = properties.ncString(WebdavEntry.PROPERTY_NB_ITEMS)?.toIntOrNull() ?: 0
        location = properties.ncString(WebdavEntry.PROPERTY_LOCATION)
        dateRange = properties.ncString(WebdavEntry.PROPERTY_DATE_RANGE)
        collaborators = properties.collaborators(baseUri + SHARE_LINK_PATH)
    }

    val albumName: String
        get() =
            URLDecoder.decode(
                href.trimEnd(PATH_SEPARATOR).substringAfterLast(PATH_SEPARATOR),
                StandardCharsets.UTF_8.name()
            )

    /**
     * Start of the album date range, falling back to now when the server did not report one.
     */
    val createdDate: Long
        get() {
            val start = dateRange?.let { runCatching { JSONObject(it).optLong(JSON_KEY_START) }.getOrNull() } ?: 0L
            return if (start > 0) start * MILLIS else System.currentTimeMillis()
        }

    private fun DavPropertySet.ncString(name: String): String? =
        this[DavPropertyName.create(name, NC_NAMESPACE)]?.value?.toString()

    private fun DavPropertySet.collaborators(shareBaseUri: String): List<Collaborator> {
        val value = this[WebdavEntry.PROPERTY_COLLABORATORS, NC_NAMESPACE]?.value
        val elements =
            when (value) {
                is Collection<*> -> value.filterIsInstance<Element>()
                is Element -> listOf(value)
                else -> emptyList()
            }
        return elements.map { it.toCollaborator(shareBaseUri) }
    }

    private fun Element.toCollaborator(shareBaseUri: String): Collaborator {
        val id = textOf(WebdavEntry.SHAREES_ID).orEmpty()
        return Collaborator(
            id = id,
            label = textOf(WebdavEntry.COLLABORATORS_SHARE_LABEL).orEmpty(),
            type =
                textOf(WebdavEntry.SHAREES_SHARE_TYPE)?.toIntOrNull()?.let(ShareType::fromValue)
                    ?: ShareType.NO_SHARED,
            shareLink = shareBaseUri + id
        )
    }

    private fun Element.textOf(tagName: String): String? =
        getElementsByTagName(tagName)
            .item(0)
            ?.firstChild
            ?.nodeValue

    companion object {
        private const val MILLIS = 1000L
        private const val PATH_SEPARATOR = '/'
        private const val JSON_KEY_START = "start"
        private const val SHARE_LINK_PATH = "/apps/photos/public/"
        private val NC_NAMESPACE = Namespace.getNamespace("nc", WebdavEntry.NAMESPACE_NC)
    }
}
