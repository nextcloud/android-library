/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.PropfindMethod
import com.owncloud.android.lib.common.network.WebdavEntry
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.files.model.RemoteFile
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.DavConstants
import org.apache.jackrabbit.webdav.MultiStatus
import org.apache.jackrabbit.webdav.property.PropfindInfo
import org.apache.jackrabbit.webdav.xml.DomUtil
import java.io.ByteArrayOutputStream

class ReadFolderRemoteOperation(
    private val remotePath: String
) : RemoteOperation<List<RemoteFile>>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<List<RemoteFile>> {
        val method =
            PropfindMethod(client.getFilesDavUri(remotePath), false, buildPropfindRequestBody(), DavConstants.DEPTH_1)

        val result =
            try {
                val status = client.execute(method)

                if (status != HttpStatus.SC_MULTI_STATUS && status != HttpStatus.SC_OK) {
                    return RemoteOperationResult(false, method)
                }

                val document = DomUtil.parseDocument(method.getResponseBodyAsStream())
                val multiStatus = MultiStatus.createFromXml(document.documentElement)
                val davUriPath = client.filesDavUri.encodedPath.orEmpty()
                RemoteOperationResult<List<RemoteFile>>(true, method).apply {
                    resultData = readData(multiStatus, davUriPath)
                }
            } catch (e: Exception) {
                RemoteOperationResult(e)
            } finally {
                method.releaseConnection()
            }

        return result.also { log(it) }
    }

    private fun buildPropfindRequestBody(): ByteArray {
        val propfindInfo = PropfindInfo(DavConstants.PROPFIND_BY_PROPERTY, WebdavUtils.getAllPropSet())
        val document = DomUtil.createDocument()
        document.appendChild(propfindInfo.toXml(document))

        return ByteArrayOutputStream().use {
            DomUtil.transformDocument(document, it)
            it.toByteArray()
        }
    }

    fun isMultiStatus(status: Int): Boolean = status == HttpStatus.SC_MULTI_STATUS

    private fun readData(
        remoteData: MultiStatus,
        davUriPath: String
    ): ArrayList<RemoteFile> {
        val responses = remoteData.responses

        return responses.mapTo(ArrayList(responses.size)) { RemoteFile(WebdavEntry(it, davUriPath)) }
    }

    @Suppress("DEPRECATION")
    private fun log(result: RemoteOperationResult<List<RemoteFile>>) {
        val message = "Synchronized $remotePath: ${result.logMessage}"
        when {
            result.isSuccess -> Log_OC.i(TAG, message)
            result.isException -> Log_OC.e(TAG, message, result.exception)
            else -> Log_OC.e(TAG, message)
        }
    }

    companion object {
        private val TAG: String = ReadFolderRemoteOperation::class.java.simpleName
    }
}
