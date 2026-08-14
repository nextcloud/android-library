/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-FileCopyrightText: 2025-2026 TSI-mc <surinder.kumar@t-systems.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.albums

import com.nextcloud.common.SessionTimeOut
import com.nextcloud.common.defaultSessionTimeOut
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.DavConstants
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod

/**
 * Reads every album of the user, or a single one when [albumRemotePath] is given.
 */
class ReadAlbumsRemoteOperation
    @JvmOverloads
    constructor(
        private val albumRemotePath: String? = null,
        private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
    ) : RemoteOperation<List<PhotoAlbumEntry>>() {
        @Deprecated("Deprecated in Java")
        @Suppress("TooGenericExceptionCaught", "DEPRECATION")
        override fun run(client: OwnCloudClient): RemoteOperationResult<List<PhotoAlbumEntry>> {
            var propFind: PropFindMethod? = null
            return try {
                val url =
                    if (albumRemotePath.isNullOrEmpty()) {
                        client.albumsDavUri
                    } else {
                        client.albumUri(albumRemotePath)
                    }
                propFind = PropFindMethod(url, WebdavUtils.getAlbumPropSet(), DavConstants.DEPTH_1)
                val status =
                    client.executeMethod(propFind, sessionTimeOut.readTimeOut, sessionTimeOut.connectionTimeOut)

                if (isMultiStatusOrOk(status)) {
                    RemoteOperationResult<List<PhotoAlbumEntry>>(true, propFind).apply {
                        resultData = propFind.albums(client)
                    }
                } else {
                    client.exhaustResponse(propFind.responseBodyAsStream)
                    RemoteOperationResult(false, propFind)
                }
            } catch (e: Exception) {
                failure(e)
            } finally {
                propFind?.releaseConnection()
            }
        }

        private fun PropFindMethod.albums(client: OwnCloudClient): List<PhotoAlbumEntry> =
            responseBodyAsMultiStatus
                .responses
                .filter { it.status?.firstOrNull()?.statusCode == HttpStatus.SC_OK }
                .map { PhotoAlbumEntry(client.baseUri.toString(), it) }

        @Suppress("DEPRECATION")
        private fun failure(e: Exception): RemoteOperationResult<List<PhotoAlbumEntry>> =
            RemoteOperationResult<List<PhotoAlbumEntry>>(e).also {
                Log_OC.e(TAG, "Read album failed: ${it.logMessage}", it.exception)
            }

        companion object {
            private val TAG: String = ReadAlbumsRemoteOperation::class.java.simpleName
        }
    }
