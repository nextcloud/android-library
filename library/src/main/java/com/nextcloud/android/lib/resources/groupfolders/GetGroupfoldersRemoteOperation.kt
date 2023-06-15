/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2023 Tobias Kaminsky
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
package com.nextcloud.android.lib.resources.groupfolders

import com.google.gson.reflect.TypeToken
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.ServerResponse
import com.owncloud.android.lib.resources.OCSNextcloudRemoteOperation
import org.apache.commons.httpclient.HttpStatus

/**
 * Get groupfolders of an user
 */
class GetGroupfoldersRemoteOperation :
    OCSNextcloudRemoteOperation<Map<String, Groupfolder>>() {
    @Suppress("TooGenericExceptionCaught")
    override fun run(client: NextcloudClient): RemoteOperationResult<Map<String, Groupfolder>> {
        var result: RemoteOperationResult<Map<String, Groupfolder>>
        var getMethod: GetMethod? = null
        try {
            getMethod =
                GetMethod(
                    client.baseUri.toString() + GROUPFOLDERS_ENDPOINT + JSON_FORMAT + APPLICABLE,
                    true
                )
            val status = client.execute(getMethod)
            if (status == HttpStatus.SC_OK) {
                val map = getServerResponse(
                    getMethod,
                    object : TypeToken<ServerResponse<Map<String, Groupfolder>>>() {}
                )
                    .ocs.data
                result = RemoteOperationResult(true, getMethod)
                result.setResultData(map)
            } else {
                result = RemoteOperationResult(false, getMethod)
            }
        } catch (e: Exception) {
            result = RemoteOperationResult(e)
            Log_OC.e(
                TAG,
                "Get groupfolders failed: " + result.logMessage,
                result.exception
            )
        } finally {
            getMethod?.releaseConnection()
        }
        return result
    }

    companion object {
        private val TAG = GetGroupfoldersRemoteOperation::class.java.simpleName
        private const val GROUPFOLDERS_ENDPOINT = "/index.php/apps/groupfolders/folders"
        private const val APPLICABLE = "&applicable=1"
    }
}
