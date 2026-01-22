/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 TSI-mc <surinder.kumar@t-systems.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.albums

import com.nextcloud.common.SessionTimeOut
import com.nextcloud.common.defaultSessionTimeOut
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.network.WebdavEntry
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.files.ToggleFavoriteRemoteOperation
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.client.methods.PropPatchMethod
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet
import org.apache.jackrabbit.webdav.property.DavPropertySet
import org.apache.jackrabbit.webdav.property.DefaultDavProperty
import org.apache.jackrabbit.webdav.xml.Namespace
import java.io.IOException

class ToggleAlbumFavoriteRemoteOperation
    @JvmOverloads
    constructor(
        private val markAsFavorite: Boolean,
        private val filePath: String,
        private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
    ) : RemoteOperation<Any>() {
        @Deprecated("Deprecated in Java")
        @Suppress("ReturnCount")
        override fun run(client: OwnCloudClient): RemoteOperationResult<Any> {
            if (filePath.isEmpty() || filePath.isBlank()) {
                return RemoteOperationResult(RemoteOperationResult.ResultCode.OK)
            }

            // when file is in local db the remotePath will be actual path instead of albums path
            // to perform operation we have to call files dav uri
            if (!filePath.contains("/albums/")) {
                return ToggleFavoriteRemoteOperation(markAsFavorite, filePath).execute(client)
            }

            var result: RemoteOperationResult<Any>
            var propPatchMethod: PropPatchMethod? = null
            val newProps = DavPropertySet()
            val removeProperties = DavPropertyNameSet()
                val favoriteProperty =
                    DefaultDavProperty<Any>(
                        "oc:favorite",
                        if(markAsFavorite) "1" else "0",
                        Namespace.getNamespace(WebdavEntry.NAMESPACE_OC)
                    )
                newProps.add(favoriteProperty)

            val webDavUrl = "${client.davUri}/photos/"
            val encodedPath = "${client.userId}${WebdavUtils.encodePath(this.filePath)}"
            val fullFilePath = "$webDavUrl$encodedPath"

            try {
                propPatchMethod = PropPatchMethod(fullFilePath, newProps, removeProperties)
                val status =
                    client.executeMethod(
                        propPatchMethod,
                        sessionTimeOut.readTimeOut,
                        sessionTimeOut.connectionTimeOut
                    )
                val isSuccess = (status == HttpStatus.SC_MULTI_STATUS || status == HttpStatus.SC_OK)
                if (isSuccess) {
                    result = RemoteOperationResult<Any>(true, status, propPatchMethod.responseHeaders)
                } else {
                    client.exhaustResponse(propPatchMethod.responseBodyAsStream)
                    result = RemoteOperationResult<Any>(false, status, propPatchMethod.responseHeaders)
                }
            } catch (e: IOException) {
                result = RemoteOperationResult<Any>(e)
            } finally {
                propPatchMethod?.releaseConnection()
            }

            return result
        }
    }
