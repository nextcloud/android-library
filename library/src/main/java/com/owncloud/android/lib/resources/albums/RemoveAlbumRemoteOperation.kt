/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 TSI-mc <surinder.kumar@t-systems.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.albums

import com.nextcloud.common.SessionTimeOut
import com.nextcloud.common.defaultSessionTimeOut
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.network.WebdavUtils
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
        /**
         * Performs the operation.
         *
         * @param client Client object to communicate with the remote ownCloud server.
         */
        @Deprecated("Deprecated in Java")
        @Suppress("TooGenericExceptionCaught")
        override fun run(client: OwnCloudClient): RemoteOperationResult<Any> {
            var result: RemoteOperationResult<Any>
            var delete: DeleteMethod? = null

            try {
                delete =
                    DeleteMethod(
                        "${client.baseUri}/remote.php/dav/photos/${client.userId}/albums${
                            WebdavUtils.encodePath(
                                albumName
                            )
                        }"
                    )
                val status =
                    client.executeMethod(
                        delete,
                        sessionTimeOut.readTimeOut,
                        sessionTimeOut.connectionTimeOut
                    )
                result =
                    RemoteOperationResult<Any>(
                        delete.succeeded() || status == HttpStatus.SC_NOT_FOUND,
                        delete
                    )
                Log_OC.i(TAG, "Remove ${this.albumName} : ${result.logMessage}")
            } catch (e: Exception) {
                result = RemoteOperationResult<Any>(e)
                Log_OC.e(TAG, "Remove ${this.albumName} : ${result.logMessage}", e)
            } finally {
                delete?.releaseConnection()
            }

            return result
        }

        companion object {
            private val TAG: String = RemoveAlbumRemoteOperation::class.java.simpleName
        }
    }
