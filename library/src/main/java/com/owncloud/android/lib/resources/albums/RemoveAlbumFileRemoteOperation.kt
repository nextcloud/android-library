/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 TSI-mc <surinder.kumar@t-systems.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package com.owncloud.android.lib.resources.albums

import android.net.Uri
import com.nextcloud.common.SessionTimeOut
import com.nextcloud.common.defaultSessionTimeOut
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.files.RemoveFileRemoteOperation
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod

class RemoveAlbumFileRemoteOperation
    @JvmOverloads
    constructor(
        private val mRemotePath: String,
        private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
    ) : RemoteOperation<Any>() {
        @Deprecated("Deprecated in Java")
        @Suppress("TooGenericExceptionCaught")
        override fun run(client: OwnCloudClient): RemoteOperationResult<Any> {
            var result: RemoteOperationResult<Any>
            var delete: DeleteMethod? = null
            val webDavUrl = "${client.davUri}/photos/"
            val encodedPath = ("${client.userId}${Uri.encode(this.mRemotePath)}").replace("%2F", "/")
            val fullFilePath = "$webDavUrl$encodedPath"

            try {
                delete = DeleteMethod(fullFilePath)
                val status =
                    client.executeMethod(
                        delete,
                        sessionTimeOut.readTimeOut,
                        sessionTimeOut.connectionTimeOut
                    )
                delete.responseBodyAsString
                result =
                    RemoteOperationResult<Any>(
                        delete.succeeded() || status == HttpStatus.SC_NOT_FOUND,
                        delete
                    )
                Log_OC.i(TAG, "Remove ${this.mRemotePath} : ${result.logMessage}")
            } catch (e: Exception) {
                result = RemoteOperationResult<Any>(e)
                Log_OC.e(TAG, "Remove ${this.mRemotePath} : ${result.logMessage}", e)
            } finally {
                delete?.releaseConnection()
            }

            return result
        }

        companion object {
            private val TAG: String = RemoveFileRemoteOperation::class.java.simpleName
        }
    }
