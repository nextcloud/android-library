/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-FileCopyrightText: 2025 TSI-mc <surinder.kumar@t-systems.com>
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
import org.apache.jackrabbit.webdav.client.methods.MkColMethod

class CreateNewAlbumRemoteOperation
    @JvmOverloads
    constructor(
        val newAlbumName: String,
        private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
    ) : RemoteOperation<Void>() {
        @Deprecated("Deprecated in Java")
        @Suppress("TooGenericExceptionCaught", "DEPRECATION")
        override fun run(client: OwnCloudClient): RemoteOperationResult<Void> {
            var mkCol: MkColMethod? = null
            return try {
                mkCol = MkColMethod(client.albumUri(newAlbumName))
                client.executeMethod(mkCol, sessionTimeOut.readTimeOut, sessionTimeOut.connectionTimeOut)

                val result =
                    if (mkCol.statusCode == HttpStatus.SC_METHOD_NOT_ALLOWED) {
                        RemoteOperationResult(ResultCode.FOLDER_ALREADY_EXISTS)
                    } else {
                        RemoteOperationResult<Void>(mkCol.succeeded(), mkCol)
                    }
                Log_OC.d(TAG, "Create album $newAlbumName : ${result.logMessage}")
                client.exhaustResponse(mkCol.responseBodyAsStream)
                result
            } catch (e: Exception) {
                failure(e)
            } finally {
                mkCol?.releaseConnection()
            }
        }

        @Suppress("DEPRECATION")
        private fun failure(e: Exception): RemoteOperationResult<Void> =
            RemoteOperationResult<Void>(e).also {
                Log_OC.e(TAG, "Create album $newAlbumName : ${it.logMessage}", e)
            }

        companion object {
            private val TAG: String = CreateNewAlbumRemoteOperation::class.java.simpleName
        }
    }
