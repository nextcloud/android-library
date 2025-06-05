/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 TSI-mc <surinder.kumar@t-systems.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package com.owncloud.android.lib.resources.albums

import android.util.Log
import com.nextcloud.common.SessionTimeOut
import com.nextcloud.common.defaultSessionTimeOut
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.DavException
import org.apache.jackrabbit.webdav.Status
import org.apache.jackrabbit.webdav.client.methods.CopyMethod
import java.io.IOException

/**
 * Remote operation moving a remote file or folder in the ownCloud server to a different folder
 * in the same account.
 *
 *
 * Allows renaming the moving file/folder at the same time.
 */
class CopyFileToAlbumRemoteOperation @JvmOverloads constructor(
    private val mSrcRemotePath: String,
    private val mTargetRemotePath: String,
    private val sessionTimeOut: SessionTimeOut = defaultSessionTimeOut
) :
    RemoteOperation<Any>() {
    /**
     * Performs the operation.
     *
     * @param client Client object to communicate with the remote ownCloud server.
     */
    @Deprecated("Deprecated in Java")
    override fun run(client: OwnCloudClient): RemoteOperationResult<Any> {
        /** check parameters */

        if (mTargetRemotePath == mSrcRemotePath) {
            // nothing to do!
            return RemoteOperationResult(ResultCode.OK)
        }

        if (mTargetRemotePath.startsWith(mSrcRemotePath)) {
            return RemoteOperationResult(ResultCode.INVALID_COPY_INTO_DESCENDANT)
        }

        /** perform remote operation */
        var copyMethod: CopyMethod? = null
        var result: RemoteOperationResult<Any>
        try {
            copyMethod = CopyMethod(
                client.getFilesDavUri(this.mSrcRemotePath),
                "${client.baseUri}/remote.php/dav/photos/${client.userId}/albums${
                    WebdavUtils.encodePath(
                        mTargetRemotePath
                    )
                }",
                false
            )
            val status = client.executeMethod(
                copyMethod,
                sessionTimeOut.readTimeOut,
                sessionTimeOut.connectionTimeOut
            )

            /** process response */
            if (status == HttpStatus.SC_MULTI_STATUS) {
                result = processPartialError(copyMethod)
            } else if (status == HttpStatus.SC_PRECONDITION_FAILED) {
                result = RemoteOperationResult<Any>(ResultCode.INVALID_OVERWRITE)
                client.exhaustResponse(copyMethod.responseBodyAsStream)
            } else {
                result = RemoteOperationResult<Any>(isSuccess(status), copyMethod)
                client.exhaustResponse(copyMethod.responseBodyAsStream)
            }

            Log.i(
                TAG,
                "Copy $mSrcRemotePath to $mTargetRemotePath : ${result.logMessage}"
            )
        } catch (e: Exception) {
            result = RemoteOperationResult<Any>(e)
            Log.e(
                TAG,
                "Copy $mSrcRemotePath to $mTargetRemotePath : ${result.logMessage}", e
            )
        } finally {
            copyMethod?.releaseConnection()
        }

        return result
    }

    /**
     * Analyzes a multistatus response from the OC server to generate an appropriate result.
     *
     *
     * In WebDAV, a COPY request on collections (folders) can be PARTIALLY successful: some
     * children are copied, some other aren't.
     *
     *
     * According to the WebDAV specification, a multistatus response SHOULD NOT include partial
     * successes (201, 204) nor for descendants of already failed children (424) in the response
     * entity. But SHOULD NOT != MUST NOT, so take carefully.
     *
     * @param copyMethod Copy operation just finished with a multistatus response
     * @return A result for the [CopyFileToAlbumRemoteOperation] caller
     * @throws java.io.IOException                       If the response body could not be parsed
     * @throws org.apache.jackrabbit.webdav.DavException If the status code is other than MultiStatus or if obtaining
     * the response XML document fails
     */
    @Throws(IOException::class, DavException::class)
    private fun processPartialError(copyMethod: CopyMethod): RemoteOperationResult<Any> {
        // Adding a list of failed descendants to the result could be interesting; or maybe not.
        // For the moment, let's take the easy way.
        /** check that some error really occurred */

        val responses = copyMethod.responseBodyAsMultiStatus.responses
        var status: Array<Status>?
        var failFound = false
        var i = 0
        while (i < responses.size && !failFound) {
            status = responses[i].status
            failFound = (!status.isNullOrEmpty() && status[0].statusCode > 299
                )
            i++
        }
        val result: RemoteOperationResult<Any> = if (failFound) {
            RemoteOperationResult<Any>(ResultCode.PARTIAL_COPY_DONE)
        } else {
            RemoteOperationResult<Any>(true, copyMethod)
        }

        return result
    }

    private fun isSuccess(status: Int): Boolean {
        return status == HttpStatus.SC_CREATED || status == HttpStatus.SC_NO_CONTENT
    }

    companion object {
        private val TAG: String = CopyFileToAlbumRemoteOperation::class.java.simpleName
    }
}
