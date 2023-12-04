/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2018 Tobias Kaminsky
 *   Copyright (C) 2018 Nextcloud GmbH
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
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
