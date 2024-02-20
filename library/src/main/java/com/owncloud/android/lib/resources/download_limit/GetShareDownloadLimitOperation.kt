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
import com.owncloud.android.lib.resources.download_limit.ShareDownloadLimitUtils.getDownloadLimitApiPath
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.methods.GetMethod

/**
 * class to fetch the download limit for the link share it requires share token to fetch the data
 *
 *
 * API : //GET to /ocs/v2.php/apps/files_downloadlimit/{share_token}/limit
 */
class GetShareDownloadLimitOperation(
    //share token from OCShare
    private val shareToken: String
) : RemoteOperation<DownloadLimitResponse>() {
    override fun run(client: OwnCloudClient): RemoteOperationResult<DownloadLimitResponse> {
        var result: RemoteOperationResult<DownloadLimitResponse>
        var status = -1
        var get: GetMethod? = null
        try {
            // Get Method
            get = GetMethod(
                client.baseUri.toString() + getDownloadLimitApiPath(
                    shareToken
                )
            )
            get.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE)
            status = client.executeMethod(get)
            if (isSuccess(status)) {
                val response = get.responseBodyAsString
                Log_OC.d(TAG, "Get Download Limit response: $response")
                val parser = DownloadLimitXMLParser()
                result = parser.parse(true, response)
                if (result.isSuccess) {
                    Log_OC.d(TAG, "Got " + result.resultData + " Response")
                }
            } else {
                result = RemoteOperationResult<DownloadLimitResponse>(false, get)
            }
        } catch (e: Exception) {
            result = RemoteOperationResult<DownloadLimitResponse>(e)
            Log_OC.e(TAG, "Exception while getting share download limit", e)
        } finally {
            get?.releaseConnection()
        }
        return result
    }

    private fun isSuccess(status: Int): Boolean {
        return status == HttpStatus.SC_OK
    }

    companion object {
        private val TAG = GetShareDownloadLimitOperation::class.java.simpleName
    }
}