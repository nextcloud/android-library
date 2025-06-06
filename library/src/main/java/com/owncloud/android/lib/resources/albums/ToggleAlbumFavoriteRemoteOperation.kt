/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 TSI-mc <surinder.kumar@t-systems.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package com.owncloud.android.lib.resources.albums

import android.net.Uri
import com.nextcloud.common.SessionTimeOut
import com.nextcloud.common.defaultSessionTimeOut
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.network.WebdavEntry
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
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
        private val makeItFavorited: Boolean,
        private val filePath: String,
        private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
    ) : RemoteOperation<Any>() {
        @Deprecated("Deprecated in Java")
        override fun run(client: OwnCloudClient): RemoteOperationResult<Any> {
            var result: RemoteOperationResult<Any>
            var propPatchMethod: PropPatchMethod? = null
            val newProps = DavPropertySet()
            val removeProperties = DavPropertyNameSet()
            if (this.makeItFavorited) {
                val favoriteProperty =
                    DefaultDavProperty<Any>(
                        "oc:favorite",
                        "1",
                        Namespace.getNamespace(WebdavEntry.NAMESPACE_OC)
                    )
                newProps.add(favoriteProperty)
            } else {
                removeProperties.add("oc:favorite", Namespace.getNamespace(WebdavEntry.NAMESPACE_OC))
            }

            val webDavUrl = "${client.davUri}/photos/"
            val encodedPath = ("${client.userId}${Uri.encode(this.filePath)}").replace("%2F", "/")
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
