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
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.common.utils.WebDavFileUtils
import com.owncloud.android.lib.resources.files.model.RemoteFile
import org.apache.jackrabbit.webdav.DavConstants
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod

class ReadAlbumItemsRemoteOperation
    @JvmOverloads
    constructor(
        private val remotePath: String,
        private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
    ) : RemoteOperation<List<RemoteFile>>() {
        @Deprecated("Deprecated in Java")
        @Suppress("TooGenericExceptionCaught")
        override fun run(client: OwnCloudClient): RemoteOperationResult<List<RemoteFile>> {
            var propFind: PropFindMethod? = null
            val result =
                try {
                    propFind =
                        PropFindMethod(
                            client.albumUri(remotePath),
                            WebdavUtils.getAlbumItemPropSet(),
                            DavConstants.DEPTH_1
                        )
                    val status =
                        client.executeMethod(propFind, sessionTimeOut.readTimeOut, sessionTimeOut.connectionTimeOut)

                    if (isMultiStatusOrOk(status)) {
                        RemoteOperationResult<List<RemoteFile>>(true, propFind).apply {
                            resultData = WebDavFileUtils().readAlbumData(propFind.responseBodyAsMultiStatus, client)
                        }
                    } else {
                        client.exhaustResponse(propFind.responseBodyAsStream)
                        RemoteOperationResult(false, propFind)
                    }
                } catch (e: OutOfMemoryError) {
                    Log_OC.e(TAG, "Not enough memory to read the content of $remotePath", e)
                    RemoteOperationResult(RemoteOperationResult.ResultCode.OUT_OF_MEMORY)
                } catch (e: Exception) {
                    RemoteOperationResult(e)
                } finally {
                    propFind?.releaseConnection()
                }

            return result.also { log(it) }
        }

        @Suppress("DEPRECATION")
        private fun log(result: RemoteOperationResult<List<RemoteFile>>) {
            val message = "Synchronized $remotePath : ${result.logMessage}"
            when {
                result.isSuccess -> Log_OC.i(TAG, message)
                result.isException -> Log_OC.e(TAG, message, result.exception)
                else -> Log_OC.e(TAG, message)
            }
        }

        companion object {
            private val TAG: String = ReadAlbumItemsRemoteOperation::class.java.simpleName
        }
    }
