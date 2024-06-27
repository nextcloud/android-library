/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2018 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import com.nextcloud.common.NextcloudClient
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode
import com.owncloud.android.lib.resources.files.webdav.NCEtag
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.apache.commons.httpclient.HttpStatus

/**
 * Check if file is up to date, by checking only eTag
 */
class CheckEtagRemoteOperation(private val path: String, private val expectedEtag: String?) :
    RemoteOperation<String?>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<String?> {
        val url = client.getFilesDavUri(path).toHttpUrl()
        val propertySet = arrayOf(NCEtag.NAME)
        val propFindMethod = com.nextcloud.operations.PropFindMethod(url, propertySet, 0)
        val propFindResult = client.execute(propFindMethod)

        return if (propFindResult.davResponse.success) {
            val etag = propFindResult.root.etag
            if (etag == expectedEtag) {
                RemoteOperationResult(ResultCode.ETAG_UNCHANGED)
            } else {
                val result = RemoteOperationResult<String?>(ResultCode.ETAG_CHANGED)
                result.resultData = etag
                result
            }
        } else if (propFindResult.davResponse.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            RemoteOperationResult(ResultCode.FILE_NOT_FOUND)
        } else {
            RemoteOperationResult(ResultCode.ETAG_CHANGED)
        }
    }
}
