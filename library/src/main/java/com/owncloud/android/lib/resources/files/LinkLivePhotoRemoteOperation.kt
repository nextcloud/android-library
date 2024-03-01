/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2018 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

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

/**
 * Links live photos
 */
class LinkLivePhotoRemoteOperation(
    private val path: String,
    private val linkedFileName: String
) : RemoteOperation<Void>() {
    @Deprecated("Deprecated in Java")
    override fun run(client: OwnCloudClient): RemoteOperationResult<Void> {
        var result: RemoteOperationResult<Void>
        lateinit var propPatchMethod: PropPatchMethod
        val newProps = DavPropertySet()
        val removeProperties = DavPropertyNameSet()
        val readMarkerProperty =
            DefaultDavProperty(
                "nc:metadata-files-live-photo",
                linkedFileName,
                Namespace.getNamespace(WebdavEntry.NAMESPACE_NC)
            )
        newProps.add(readMarkerProperty)

        val commentsPath = client.getFilesDavUri(path)
        try {
            propPatchMethod = PropPatchMethod(commentsPath, newProps, removeProperties)
            val status = client.executeMethod(propPatchMethod)
            val isSuccess =
                status == HttpStatus.SC_NO_CONTENT || status == HttpStatus.SC_OK || status == HttpStatus.SC_MULTI_STATUS
            result =
                if (isSuccess) {
                    RemoteOperationResult<Void>(true, status, propPatchMethod.responseHeaders)
                } else {
                    client.exhaustResponse(propPatchMethod.responseBodyAsStream)
                    RemoteOperationResult<Void>(false, status, propPatchMethod.responseHeaders)
                }
        } catch (e: IOException) {
            result = RemoteOperationResult<Void>(e)
        } finally {
            propPatchMethod.releaseConnection()
        }
        return result
    }
}
