/*
 * Nextcloud Android Library
 *
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

class ReadFolderRemoteOperation(private val remotePath: String?) : RemoteOperation<Any?>() {
    private var folderAndFiles: ArrayList<Any?>? = null

    override fun run(client: OwnCloudClient): RemoteOperationResult<*> {
        var result: RemoteOperationResult<*>? = null
        var query: PropFindMethod? = null

        try {
            query = PropFindMethod(
                client.getFilesDavUri(remotePath),
                WebdavUtils.getAllPropSet(),
                DavConstants.DEPTH_1
            )
            val status = client.executeMethod(query)

            val isSuccess = (status == HttpStatus.SC_MULTI_STATUS || status == HttpStatus.SC_OK)

            if (isSuccess) {
                val dataInServer = query.getResponseBodyAsMultiStatus()
                readData(dataInServer, client)
                result = RemoteOperationResult<Any?>(true, query)
                if (result.isSuccess) {
                    result.data = folderAndFiles
                }
            } else {
                client.exhaustResponse(query.getResponseBodyAsStream())
                result = RemoteOperationResult<Any?>(false, query)
            }
        } catch (e: OutOfMemoryError) {
            folderAndFiles = null
            Log_OC.e(TAG, "Not enough memory to read the content of $remotePath", e)
            result = RemoteOperationResult<Any?>(RemoteOperationResult.ResultCode.OUT_OF_MEMORY)
        } catch (e: Exception) {
            result = RemoteOperationResult<Any?>(e)
        } finally {
            query?.releaseConnection()

            if (result == null) {
                result = RemoteOperationResult<Any?>(Exception("unknown error"))
                Log_OC.e(TAG, "Synchronized $remotePath: failed")
            } else {
                if (result.isSuccess) {
                    Log_OC.i(TAG, "Synchronized " + remotePath + ": " + result.getLogMessage())
                } else {
                    if (result.isException) {
                        Log_OC.e(
                            TAG, "Synchronized " + remotePath + ": " + result.getLogMessage(),
                            result.exception
                        )
                    } else {
                        Log_OC.e(TAG, "Synchronized " + remotePath + ": " + result.getLogMessage())
                    }
                }
            }
        }

        return result
    }

    fun isMultiStatus(status: Int): Boolean {
        return (status == HttpStatus.SC_MULTI_STATUS)
    }

    private fun readData(remoteData: MultiStatus, client: OwnCloudClient) {
        val responses = remoteData.responses
        val davUriPath = client.filesDavUri.encodedPath
        folderAndFiles = ArrayList(responses.size)

        var we = WebdavEntry(responses[0]!!, davUriPath!!)
        folderAndFiles!!.add(RemoteFile(we))

        var remoteFile: RemoteFile?
        for (i in 1..<responses.size) {
            we = WebdavEntry(responses[i]!!, davUriPath)
            remoteFile = RemoteFile(we)
            folderAndFiles!!.add(remoteFile)
        }
    }

    companion object {
        private val TAG: String = ReadFolderRemoteOperation::class.java.getSimpleName()
    }
}
