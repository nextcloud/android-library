/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.albums

import com.nextcloud.common.SessionTimeOut
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.client.methods.PropPatchMethod
import org.apache.jackrabbit.webdav.property.DavProperty
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet
import org.apache.jackrabbit.webdav.property.DavPropertySet
import java.io.IOException

internal const val ALBUMS_PATH = "/albums"

private const val PHOTOS_PATH = "/photos/"

/**
 * Root of the photos DAV endpoint: `…/remote.php/dav/photos/<user>`.
 */
internal val OwnCloudClient.photosDavUri: String
    get() = "$davUri$PHOTOS_PATH$userId"

internal val OwnCloudClient.albumsDavUri: String
    get() = "$photosDavUri$ALBUMS_PATH"

internal fun OwnCloudClient.albumUri(albumPath: String): String = "$albumsDavUri${WebdavUtils.encodePath(albumPath)}"

internal fun OwnCloudClient.photosUri(remotePath: String): String = "$photosDavUri${WebdavUtils.encodePath(remotePath)}"

/**
 * The photos endpoint answers a successful PROPFIND/PROPPATCH with either 207 or 200.
 */
internal fun isMultiStatusOrOk(status: Int): Boolean =
    status == HttpStatus.SC_MULTI_STATUS || status == HttpStatus.SC_OK

/**
 * PROPPATCHes a single property onto a photos DAV resource.
 */
internal fun OwnCloudClient.setPhotosProperty(
    uri: String,
    property: DavProperty<*>,
    sessionTimeOut: SessionTimeOut
): RemoteOperationResult<Any> {
    var propPatch: PropPatchMethod? = null
    return try {
        val newProperties = DavPropertySet().apply { add(property) }
        propPatch = PropPatchMethod(uri, newProperties, DavPropertyNameSet())
        val status = executeMethod(propPatch, sessionTimeOut.readTimeOut, sessionTimeOut.connectionTimeOut)
        val success = isMultiStatusOrOk(status)
        if (!success) {
            exhaustResponse(propPatch.responseBodyAsStream)
        }
        RemoteOperationResult(success, status, propPatch.responseHeaders)
    } catch (e: IOException) {
        RemoteOperationResult(e)
    } finally {
        propPatch?.releaseConnection()
    }
}
