/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.network.WebdavEntry
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.files.model.RemoteFile
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.DavConstants
import org.apache.jackrabbit.webdav.MultiStatus
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod

class ReadFolderRemoteOperation(
    private val remotePath: String
) : RemoteOperation<Any>() {
    @Deprecated("Deprecated in Java")
    @Suppress("TooGenericExceptionCaught", "DEPRECATION")
    override fun run(client: OwnCloudClient): RemoteOperationResult<Any> {
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
                    RemoteOperationResult<Any>(true, query).apply { data = folderAndFiles }
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

    fun isMultiStatus(status: Int): Boolean = status == HttpStatus.SC_MULTI_STATUS

    private fun readData(
        remoteData: MultiStatus,
        client: OwnCloudClient
    ): ArrayList<Any> {
        val responses = remoteData.responses
        val davUriPath = client.filesDavUri.encodedPath.orEmpty()

        return responses.mapTo(ArrayList(responses.size)) { RemoteFile(WebdavEntry(it, davUriPath)) }
    }

    @Suppress("DEPRECATION")
    private fun log(result: RemoteOperationResult<Any>) {
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
