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
import com.owncloud.android.lib.common.utils.Log_OC
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod

class RemoveAlbumRemoteOperation
    @JvmOverloads
    constructor(
        private val albumName: String,
        private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
    ) : RemoteOperation<Any>() {
        @Deprecated("Deprecated in Java")
        @Suppress("TooGenericExceptionCaught", "DEPRECATION")
        override fun run(client: OwnCloudClient): RemoteOperationResult<Any> {
            var delete: DeleteMethod? = null
            return try {
                delete = DeleteMethod(client.albumUri(albumName))
                val status = client.executeMethod(delete, sessionTimeOut.readTimeOut, sessionTimeOut.connectionTimeOut)

                // an already missing album is not an error for the caller
                val removed = delete.succeeded() || status == HttpStatus.SC_NOT_FOUND
                RemoteOperationResult<Any>(removed, delete).also {
                    Log_OC.i(TAG, "Remove $albumName : ${it.logMessage}")
                }
            } catch (e: Exception) {
                failure(e)
            } finally {
                delete?.releaseConnection()
            }
        }

        @Suppress("DEPRECATION")
        private fun failure(e: Exception): RemoteOperationResult<Any> =
            RemoteOperationResult<Any>(e).also {
                Log_OC.e(TAG, "Remove $albumName : ${it.logMessage}", e)
            }

        companion object {
            private val TAG: String = RemoveAlbumRemoteOperation::class.java.simpleName
        }
    }
