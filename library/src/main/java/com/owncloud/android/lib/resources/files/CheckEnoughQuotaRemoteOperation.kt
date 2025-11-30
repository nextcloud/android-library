/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2023 Tobias Kaminsky
 *   Copyright (C) 2023 Nextcloud GmbH
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
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode
import com.owncloud.android.lib.common.utils.Log_OC
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.DavException
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod
import org.apache.jackrabbit.webdav.property.DavPropertyName
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet
import java.io.File
import java.io.IOException

/**
 * Check if remaining quota is big enough
 * @param fileSize filesize in bytes
 */
class CheckEnoughQuotaRemoteOperation(val path: String, private val fileSize: Long) :
    RemoteOperation<Boolean>() {
    @Deprecated("Deprecated in Java")
    @Suppress("Detekt.ReturnCount")
    override fun run(client: OwnCloudClient): RemoteOperationResult<Boolean> {
        var propfind: PropFindMethod? = null
        try {
            val file = File(path)
            val folder =
                if (file.path.endsWith(FileUtils.PATH_SEPARATOR)) {
                    file.path
                } else {
                    file.parent ?: throw IllegalStateException("Parent path not found")
                }

            val propSet = DavPropertyNameSet()
            propSet.add(QUOTA_PROPERTY)
            propfind =
                PropFindMethod(
                    client.getFilesDavUri(folder),
                    propSet,
                    0
                )
            val status = client.executeMethod(propfind, SYNC_READ_TIMEOUT, SYNC_CONNECTION_TIMEOUT)
            if (status == HttpStatus.SC_MULTI_STATUS || status == HttpStatus.SC_OK) {
                val resp = propfind.responseBodyAsMultiStatus.responses[0]
                val string = resp.getProperties(HttpStatus.SC_OK)[QUOTA_PROPERTY].value as String
                val quota = string.toLong()
                return if (isSuccess(quota)) {
                    RemoteOperationResult<Boolean>(true, propfind)
                } else {
                    RemoteOperationResult<Boolean>(false, propfind)
                }
            }
            if (status == HttpStatus.SC_NOT_FOUND) {
                return RemoteOperationResult(ResultCode.FILE_NOT_FOUND)
            }
        } catch (e: DavException) {
            Log_OC.e(TAG, "Error while retrieving quota")
        } catch (e: IOException) {
            Log_OC.e(TAG, "Error while retrieving quota")
        } catch (e: NumberFormatException) {
            Log_OC.e(TAG, "Error while retrieving quota")
        } finally {
            propfind?.releaseConnection()
        }
        return RemoteOperationResult(ResultCode.ETAG_CHANGED)
    }

    private fun isSuccess(quota: Long): Boolean {
        return quota >= fileSize ||
            quota == UNKNOWN_FREE_SPACE ||
            quota == UNCOMPUTED_FREE_SPACE ||
            quota == UNLIMITED_FREE_SPACE
    }

    companion object {
        private const val SYNC_READ_TIMEOUT = 40000
        private const val SYNC_CONNECTION_TIMEOUT = 5000
        private const val UNCOMPUTED_FREE_SPACE = -1L
        private const val UNKNOWN_FREE_SPACE = -2L
        private const val UNLIMITED_FREE_SPACE = -3L
        private val QUOTA_PROPERTY = DavPropertyName.create(WebdavEntry.PROPERTY_QUOTA_AVAILABLE_BYTES)
        private val TAG = CheckEnoughQuotaRemoteOperation::class.java.simpleName
    }
}
