/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 TSI-mc <surinder.kumar@t-systems.com>
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
import org.apache.jackrabbit.webdav.client.methods.MkColMethod

class CreateNewAlbumRemoteOperation
    @JvmOverloads
    constructor(
        val newAlbumName: String,
        private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
    ) : RemoteOperation<Void>() {
        /**
         * Performs the operation.
         *
         * @param client Client object to communicate with the remote ownCloud server.
         */
        @Deprecated("Deprecated in Java")
        @Suppress("TooGenericExceptionCaught")
        override fun run(client: OwnCloudClient): RemoteOperationResult<Void> {
            var mkCol: MkColMethod? = null
            var result: RemoteOperationResult<Void>
            try {
                mkCol =
                    MkColMethod(
                        "${client.baseUri}/remote.php/dav/photos/${client.userId}/albums${
                            WebdavUtils.encodePath(
                                newAlbumName
                            )
                        }"
                    )
                client.executeMethod(
                    mkCol,
                    sessionTimeOut.readTimeOut,
                    sessionTimeOut.connectionTimeOut
                )
                if (HttpStatus.SC_METHOD_NOT_ALLOWED == mkCol.statusCode) {
                    result =
                        RemoteOperationResult(RemoteOperationResult.ResultCode.FOLDER_ALREADY_EXISTS)
                } else {
                    result = RemoteOperationResult(mkCol.succeeded(), mkCol)
                    result.resultData = null
                }

                Log_OC.d(TAG, "Create album $newAlbumName : ${result.logMessage}")
                client.exhaustResponse(mkCol.responseBodyAsStream)
            } catch (e: Exception) {
                result = RemoteOperationResult(e)
                Log_OC.e(TAG, "Create album $newAlbumName : ${result.logMessage}", e)
            } finally {
                mkCol?.releaseConnection()
            }

            return result
        }

        companion object {
            private val TAG: String = CreateNewAlbumRemoteOperation::class.java.simpleName
        }
    }
