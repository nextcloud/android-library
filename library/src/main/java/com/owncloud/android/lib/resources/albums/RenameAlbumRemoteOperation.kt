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
import org.apache.jackrabbit.webdav.client.methods.MoveMethod

class RenameAlbumRemoteOperation
@JvmOverloads
constructor(
    private val mOldRemotePath: String,
    val newAlbumName: String,
    private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
) : RemoteOperation<Any>() {
    /**
     * Performs the operation.
     *
     * @param client Client object to communicate with the remote ownCloud server.
     */
    @Deprecated("Deprecated in Java")
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: OwnCloudClient): RemoteOperationResult<Any>? {
        if (this.newAlbumName == this.mOldRemotePath) {
            return RemoteOperationResult(RemoteOperationResult.ResultCode.OK)
        }

        var result: RemoteOperationResult<Any>? = null
        var move: MoveMethod? = null
        val url = "${client.baseUri}/remote.php/dav/photos/${client.userId}/albums"
        try {
            move =
                MoveMethod(
                    "$url${WebdavUtils.encodePath(mOldRemotePath)}",
                    "$url${
                        WebdavUtils.encodePath(
                            newAlbumName
                        )
                    }",
                    false
                )
            client.executeMethod(
                move,
                sessionTimeOut.readTimeOut,
                sessionTimeOut.connectionTimeOut
            )
            result = RemoteOperationResult<Any>(move.succeeded(), move)
            Log_OC.i(
                TAG,
                "Rename ${this.mOldRemotePath} to ${this.newAlbumName} : ${result.logMessage}"
            )
            // album name already exist
            if (!result.isSuccess && result.httpCode == HttpStatus.SC_PRECONDITION_FAILED) {
                result = RemoteOperationResult<Any>(RemoteOperationResult.ResultCode.INVALID_OVERWRITE)
            }
            client.exhaustResponse(move.responseBodyAsStream)
        } catch (e: Exception) {
            result = RemoteOperationResult<Any>(e)
            Log_OC.e(
                TAG,
                "Rename ${this.mOldRemotePath} to ${this.newAlbumName} : ${result.logMessage}",
                e
            )
        } finally {
            move?.releaseConnection()
        }

        return result
    }

    companion object {
        private val TAG: String = RenameAlbumRemoteOperation::class.java.simpleName
    }
}
