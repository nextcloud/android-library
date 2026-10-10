/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import androidx.core.net.toUri
import at.bitfire.dav4jvm.DavCollection
import at.bitfire.dav4jvm.Response
import com.nextcloud.common.NextcloudClient
import com.nextcloud.common.WebDavUtils
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.network.WebdavEntry
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.files.model.RemoteFile
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.DavConstants
import org.apache.jackrabbit.webdav.MultiStatus
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod

class ReadFolderRemoteOperation(
    private val remotePath: String
) : RemoteOperation<List<RemoteFile>>() {
    @Deprecated("Deprecated in Java")
    @Suppress("TooGenericExceptionCaught", "DEPRECATION")
    override fun run(client: OwnCloudClient): RemoteOperationResult<List<RemoteFile>> {
        var query: PropFindMethod? = null

        val result =
            try {
                query =
                    PropFindMethod(
                        client.getFilesDavUri(remotePath),
                        WebdavUtils.getAllPropSet(),
                        DavConstants.DEPTH_1
                    )
                val status = client.executeMethod(query)

                if (status == HttpStatus.SC_MULTI_STATUS || status == HttpStatus.SC_OK) {
                    val folderAndFiles = readData(query.responseBodyAsMultiStatus, client)
                    RemoteOperationResult<List<RemoteFile>>(true, query).apply { resultData = folderAndFiles }
                } else {
                    client.exhaustResponse(query.responseBodyAsStream)
                    RemoteOperationResult(false, query)
                }
            } catch (e: OutOfMemoryError) {
                Log_OC.e(TAG, "Not enough memory to read the content of $remotePath", e)
                RemoteOperationResult(RemoteOperationResult.ResultCode.OUT_OF_MEMORY)
            } catch (e: Exception) {
                RemoteOperationResult(e)
            } finally {
                query?.releaseConnection()
            }

        return result.also { log(it) }
    }

    @Suppress("SpreadOperator", "Detekt.TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<List<RemoteFile>> {
        WebDavUtils.registerCustomFactories()

        val result: MutableList<RemoteFile> = mutableListOf()
        val location = client.getFilesDavUri(remotePath).toHttpUrl()

        val davCollection = DavCollection(client.disabledRedirectClient(), location)
        val filesDavUri = client.getFilesDavUri("/").toUri()

        try {
            davCollection.propfind(depth = 1, *WebDavUtils.PROPERTYSETS.ALL) { response, hrefRelation ->
                if (response.isSuccess()) {
                    when (hrefRelation) {
                        Response.HrefRelation.MEMBER -> {
                            result.add(WebDavUtils.parseResponse(response, filesDavUri))
                        }

                        Response.HrefRelation.SELF, Response.HrefRelation.OTHER -> {
                            result.add(WebDavUtils.parseResponse(response, filesDavUri))
                        }

                        else -> {}
                    }
                }
            }

            return if (result == null) {
                RemoteOperationResult<List<RemoteFile>>(RemoteOperationResult.ResultCode.UNKNOWN_ERROR)
            } else {
                RemoteOperationResult<List<RemoteFile>>(RemoteOperationResult.ResultCode.OK).apply {
                    resultData = result
                }
            }
        } catch (e: Exception) {
            return RemoteOperationResult<List<RemoteFile>>(e)
        }
    }

    private fun readData(
        remoteData: MultiStatus,
        client: OwnCloudClient
    ): ArrayList<RemoteFile> {
        val responses = remoteData.responses
        val davUriPath = client.filesDavUri.encodedPath.orEmpty()

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
