/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 TSI-mc <surinder.kumar@t-systems.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package com.owncloud.android.lib.resources.albums

import android.text.TextUtils
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

class ReadAlbumsRemoteOperation
    @JvmOverloads
    constructor(
        private val mAlbumRemotePath: String? = null,
        private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
    ) : RemoteOperation<List<PhotoAlbumEntry>>() {
        /**
         * Performs the operation.
         *
         * @param client Client object to communicate with the remote ownCloud server.
         */
        @Deprecated("Deprecated in Java")
        @Suppress("TooGenericExceptionCaught")
        override fun run(client: OwnCloudClient): RemoteOperationResult<List<PhotoAlbumEntry>> {
            var propfind: PropFindMethod? = null
            var result: RemoteOperationResult<List<PhotoAlbumEntry>>
            var url = "${client.baseUri}/remote.php/dav/photos/${client.userId}/albums"
            if (!TextUtils.isEmpty(mAlbumRemotePath)) {
                url += WebdavUtils.encodePath(mAlbumRemotePath)
            }
            try {
                propfind = PropFindMethod(url, WebdavUtils.getAlbumPropSet(), DavConstants.DEPTH_1)
                val status =
                    client.executeMethod(
                        propfind,
                        sessionTimeOut.readTimeOut,
                        sessionTimeOut.connectionTimeOut
                    )
                val isSuccess = status == HttpStatus.SC_MULTI_STATUS || status == HttpStatus.SC_OK
                if (isSuccess) {
                    val albumsList =
                        propfind.responseBodyAsMultiStatus.responses
                            .filter { it.status[0].statusCode == HttpStatus.SC_OK }
                            .map { res -> PhotoAlbumEntry(res) }
                    result = RemoteOperationResult<List<PhotoAlbumEntry>>(true, propfind)
                    result.resultData = albumsList
                } else {
                    result = RemoteOperationResult<List<PhotoAlbumEntry>>(false, propfind)
                    client.exhaustResponse(propfind.responseBodyAsStream)
                }
            } catch (e: Exception) {
                result = RemoteOperationResult<List<PhotoAlbumEntry>>(e)
                Log_OC.e(TAG, "Read album failed: ${result.logMessage}", result.exception)
            } finally {
                propfind?.releaseConnection()
            }

            return result
        }

        companion object {
            private val TAG: String = ReadAlbumsRemoteOperation::class.java.simpleName
        }
    }
