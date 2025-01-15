/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 ZetaTom <70907959+zetatom@users.noreply.github.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.files

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.DeleteMethod
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.OCSRemoteOperation
import org.apache.commons.httpclient.HttpStatus

class RemoveFilesDownloadLimitRemoteOperation(
    val token: String
) : OCSRemoteOperation<Void>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<Void> {
        val result: RemoteOperationResult<Void>

        val url = client.baseUri.toString() + String.format(FILES_DOWNLOAD_LIMIT_ENDPOINT, token) + JSON_FORMAT
        val deleteMethod = DeleteMethod(url, true)

        val status = deleteMethod.execute(client)

        if (status == HttpStatus.SC_OK) {
            result = RemoteOperationResult(true, deleteMethod)
        } else {
            result = RemoteOperationResult(false, deleteMethod)
            Log_OC.e(TAG, "Failed to remove download limit")
            Log_OC.e(TAG, "*** status code: " + status + "; response: " + deleteMethod.getResponseBodyAsString())
        }

        deleteMethod.releaseConnection()

        return result
    }

    companion object {
        private val TAG = RemoveFilesDownloadLimitRemoteOperation::class.java.simpleName
        private const val FILES_DOWNLOAD_LIMIT_ENDPOINT = "/ocs/v2.php/apps/files_downloadlimit/api/v1/%s/limit"
    }
}
