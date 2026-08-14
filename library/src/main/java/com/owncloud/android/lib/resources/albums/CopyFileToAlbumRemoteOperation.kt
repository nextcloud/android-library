/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-FileCopyrightText: 2026 TSI-mc <surinder.kumar@t-systems.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.albums

import com.nextcloud.common.SessionTimeOut
import com.nextcloud.common.defaultSessionTimeOut
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode
import com.owncloud.android.lib.common.utils.Log_OC
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.DavException
import org.apache.jackrabbit.webdav.client.methods.CopyMethod
import java.io.IOException

class CopyFileToAlbumRemoteOperation
    @JvmOverloads
    constructor(
        private val srcRemotePath: String,
        private val targetRemotePath: String,
        private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
    ) : RemoteOperation<Any>() {
        @Deprecated("Deprecated in Java")
        @Suppress("TooGenericExceptionCaught", "DEPRECATION")
        override fun run(client: OwnCloudClient): RemoteOperationResult<Any> {
            if (targetRemotePath == srcRemotePath) {
                return RemoteOperationResult(ResultCode.OK)
            }

            var copy: CopyMethod? = null
            return try {
                copy = CopyMethod(client.getFilesDavUri(srcRemotePath), client.albumUri(targetRemotePath), false)
                val status = client.executeMethod(copy, sessionTimeOut.readTimeOut, sessionTimeOut.connectionTimeOut)

                when (status) {
                    HttpStatus.SC_MULTI_STATUS -> {
                        processPartialError(copy)
                    }

                    HttpStatus.SC_PRECONDITION_FAILED -> {
                        client.exhaustResponse(copy.responseBodyAsStream)
                        RemoteOperationResult(ResultCode.INVALID_OVERWRITE)
                    }

                    else -> {
                        client.exhaustResponse(copy.responseBodyAsStream)
                        RemoteOperationResult<Any>(isSuccess(status), copy)
                    }
                }.also {
                    Log_OC.i(TAG, "Copy $srcRemotePath to $targetRemotePath : ${it.logMessage}")
                }
            } catch (e: Exception) {
                failure(e)
            } finally {
                copy?.releaseConnection()
            }
        }

        /**
         * A COPY of a collection can be partially successful: some children are copied, some are not.
         */
        @Throws(IOException::class, DavException::class)
        private fun processPartialError(copy: CopyMethod): RemoteOperationResult<Any> {
            val partiallyFailed =
                copy.responseBodyAsMultiStatus.responses.any { response ->
                    val statusCode = response.status?.firstOrNull()?.statusCode ?: 0
                    statusCode > MAX_SUCCESS_STATUS_CODE
                }

            return if (partiallyFailed) {
                RemoteOperationResult(ResultCode.PARTIAL_COPY_DONE)
            } else {
                RemoteOperationResult<Any>(true, copy)
            }
        }

        private fun isSuccess(status: Int): Boolean =
            status == HttpStatus.SC_CREATED || status == HttpStatus.SC_NO_CONTENT

        @Suppress("DEPRECATION")
        private fun failure(e: Exception): RemoteOperationResult<Any> =
            RemoteOperationResult<Any>(e).also {
                Log_OC.e(TAG, "Copy $srcRemotePath to $targetRemotePath : ${it.logMessage}", e)
            }

        companion object {
            private val TAG: String = CopyFileToAlbumRemoteOperation::class.java.simpleName
            private const val MAX_SUCCESS_STATUS_CODE = 299
        }
    }
