/* Nextcloud Android Library is available under MIT license
 *
 *   @author TSI-mc
 *   Copyright (C) 2023 TSI-mc
 *   Copyright (C) 2023 Nextcloud GmbH
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 */

package com.owncloud.android.lib.resources.download_limit

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.download_limit.model.DownloadLimitResponse
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.methods.DeleteMethod

/**
 * class to delete the download limit for the link share
 * this has to be executed when user has toggled off the download limit
 *
 *
 * API : //DELETE to /ocs/v2.php/apps/files_downloadlimit/{share_token}/limit
 */
class DeleteShareDownloadLimitRemoteOperation(private val shareToken: String) :
    RemoteOperation<DownloadLimitResponse>() {
    override fun run(client: OwnCloudClient): RemoteOperationResult<DownloadLimitResponse> {
        var result: RemoteOperationResult<DownloadLimitResponse>
        val status: Int
        var deleteMethod: DeleteMethod? = null
        try {
            // Post Method
            deleteMethod = DeleteMethod(
                client.baseUri.toString() + ShareDownloadLimitUtils.getDownloadLimitApiPath(
                    shareToken
                )
            )
            deleteMethod.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE)
            status = client.executeMethod(deleteMethod)
            if (isSuccess(status)) {
                val response = deleteMethod.responseBodyAsString
                Log_OC.d(TAG, "Delete Download Limit response: $response")
                val parser = DownloadLimitXMLParser()
                result = parser.parse(true, response)
                if (result.isSuccess) {
                    return result
                }
            } else {
                result = RemoteOperationResult<DownloadLimitResponse>(false, deleteMethod)
            }
        } catch (e: Exception) {
            result = RemoteOperationResult<DownloadLimitResponse>(e)
            Log_OC.e(TAG, "Exception while deleting share download limit", e)
        } finally {
            deleteMethod?.releaseConnection()
        }
        return result
    }

    private fun isSuccess(status: Int): Boolean {
        return status == HttpStatus.SC_OK || status == HttpStatus.SC_BAD_REQUEST
    }

    companion object {
        private val TAG = DeleteShareDownloadLimitRemoteOperation::class.java.simpleName
    }
}