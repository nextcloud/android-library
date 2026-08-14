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
import org.apache.jackrabbit.webdav.client.methods.MoveMethod

class RenameAlbumRemoteOperation
    @JvmOverloads
    constructor(
        private val oldRemotePath: String,
        val newAlbumName: String,
        private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
    ) : RemoteOperation<Any>() {
        @Deprecated("Deprecated in Java")
        @Suppress("TooGenericExceptionCaught", "DEPRECATION")
        override fun run(client: OwnCloudClient): RemoteOperationResult<Any> {
            if (newAlbumName == oldRemotePath) {
                return RemoteOperationResult(ResultCode.OK)
            }

            var move: MoveMethod? = null
            return try {
                move = MoveMethod(client.albumUri(oldRemotePath), client.albumUri(newAlbumName), false)
                client.executeMethod(move, sessionTimeOut.readTimeOut, sessionTimeOut.connectionTimeOut)

                val renamed = RemoteOperationResult<Any>(move.succeeded(), move)
                Log_OC.i(TAG, "Rename $oldRemotePath to $newAlbumName : ${renamed.logMessage}")

                val result =
                    if (renamed.httpCode == HttpStatus.SC_PRECONDITION_FAILED) {
                        // the target album name is already taken
                        RemoteOperationResult(ResultCode.INVALID_OVERWRITE)
                    } else {
                        renamed
                    }
                client.exhaustResponse(move.responseBodyAsStream)
                result
            } catch (e: Exception) {
                failure(e)
            } finally {
                move?.releaseConnection()
            }
        }

        @Suppress("DEPRECATION")
        private fun failure(e: Exception): RemoteOperationResult<Any> =
            RemoteOperationResult<Any>(e).also {
                Log_OC.e(TAG, "Rename $oldRemotePath to $newAlbumName : ${it.logMessage}", e)
            }

        companion object {
            private val TAG: String = RenameAlbumRemoteOperation::class.java.simpleName
        }
    }
