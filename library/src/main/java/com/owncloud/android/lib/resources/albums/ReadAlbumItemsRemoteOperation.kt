/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 TSI-mc <surinder.kumar@t-systems.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
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
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.DavConstants
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod

class ReadAlbumItemsRemoteOperation @JvmOverloads constructor(
    private val mRemotePath: String,
    private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
) :
    RemoteOperation<List<RemoteFile>>() {
    @Deprecated("Deprecated in Java")
    override fun run(client: OwnCloudClient): RemoteOperationResult<List<RemoteFile>> {
        var result: RemoteOperationResult<List<RemoteFile>>? = null
        var query: PropFindMethod? = null
        val url = "${client.baseUri}/remote.php/dav/photos/${client.userId}/albums${
            WebdavUtils.encodePath(
                mRemotePath
            )
        }"
        try {
            // remote request
            query = PropFindMethod(
                url,
                WebdavUtils.getAllPropSet(),  // PropFind Properties
                DavConstants.DEPTH_1
            )
            val status = client.executeMethod(
                query,
                sessionTimeOut.readTimeOut,
                sessionTimeOut.connectionTimeOut
            )

            // check and process response
            val isSuccess = (status == HttpStatus.SC_MULTI_STATUS || status == HttpStatus.SC_OK)

            if (isSuccess) {
                // get data from remote folder
                val dataInServer = query.responseBodyAsMultiStatus
                val mFolderAndFiles = WebDavFileUtils().readAlbumData(dataInServer, client)

                // Result of the operation
                result = RemoteOperationResult(true, query)
                // Add data to the result
                if (result.isSuccess) {
                    result.resultData = mFolderAndFiles
                }
            } else {
                // synchronization failed
                client.exhaustResponse(query.responseBodyAsStream)
                result = RemoteOperationResult(false, query)
            }
        } catch (e: Exception) {
            result = RemoteOperationResult(e)
        } finally {
            query?.releaseConnection()

            if (result == null) {
                result = RemoteOperationResult<List<RemoteFile>>(Exception("unknown error"))
                Log_OC.e(
                    TAG,
                    "Synchronized $mRemotePath: failed"
                )
            } else {
                if (result.isSuccess) {
                    Log_OC.i(TAG, "Synchronized $mRemotePath : ${result.logMessage}")
                } else {
                    if (result.isException) {
                        Log_OC.e(
                            TAG, "Synchronized $mRemotePath : ${result.logMessage}",
                            result.exception
                        )
                    } else {
                        Log_OC.e(TAG, "Synchronized $mRemotePath : ${result.logMessage}")
                    }
                }
            }
        }

        return result
    }

    companion object {
        private val TAG: String = ReadAlbumItemsRemoteOperation::class.java.simpleName
    }
}
