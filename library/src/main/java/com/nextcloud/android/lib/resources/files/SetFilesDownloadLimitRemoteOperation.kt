/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 ZetaTom <70907959+zetatom@users.noreply.github.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.files

import com.nextcloud.common.JSONRequestBody
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.PutMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.OCSRemoteOperation
import org.apache.commons.httpclient.HttpStatus

class SetFilesDownloadLimitRemoteOperation(
    val token: String,
    val limit: Int
) : OCSRemoteOperation<Void>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<Void> {
        val result: RemoteOperationResult<Void>

        val url = client.baseUri.toString() + String.format(FILES_DOWNLOAD_LIMIT_ENDPOINT, token)
        val jsonRequestBody = JSONRequestBody("limit", limit.toString())
        val putMethod = PutMethod(url, true, jsonRequestBody.get())

        val status = putMethod.execute(client)

        if (status == HttpStatus.SC_OK) {
            result = RemoteOperationResult(true, putMethod)
        } else {
            result = RemoteOperationResult(false, putMethod)
            Log_OC.e(TAG, "Failed to set download limit")
            Log_OC.e(TAG, "*** status code: " + status + "; response: " + putMethod.getResponseBodyAsString())
        }

        putMethod.releaseConnection()

        return result
    }

    companion object {
        private val TAG = SetFilesDownloadLimitRemoteOperation::class.java.simpleName
        private const val FILES_DOWNLOAD_LIMIT_ENDPOINT = "/ocs/v2.php/apps/files_downloadlimit/api/v1/%s/limit"
    }
}
