/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.WebDavFileUtils
import com.owncloud.android.lib.resources.files.model.RemoteFile
import com.owncloud.android.lib.resources.status.OCCapability
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.client.methods.OptionsMethod
import org.apache.jackrabbit.webdav.search.SearchInfo
import org.apache.jackrabbit.webdav.xml.Namespace

class SearchRemoteOperation(
    private val searchQuery: String?,
    private val searchType: SearchType?,
    private val filterOutFiles: Boolean,
    private val capability: OCCapability?
) : RemoteOperation<MutableList<RemoteFile?>?>() {
    enum class SearchType {
        FILE_SEARCH,
        FAVORITE_SEARCH,
        RECENTLY_MODIFIED_SEARCH,
        PHOTO_SEARCH,

        @Deprecated("unused, to be removed in a future version")
        SHARED_SEARCH,
        GALLERY_SEARCH,
        FILE_ID_SEARCH,
        CONTENT_TYPE_SEARCH,
        RECENTLY_ADDED_SEARCH,
        SHARED_FILTER
    }

    var limit: Int = 0
    var timestamp: Long = -1
    var startDate: Long? = null
    var endDate: Long? = null

    @Deprecated("Deprecated in Java")
    @Suppress("Detekt.TooGenericExceptionCaught", "DEPRECATION")
    override fun run(client: OwnCloudClient): RemoteOperationResult<MutableList<RemoteFile?>?> {
        val webDavUrl = client.davUri.toString()
        val optionsMethod = OptionsMethod(webDavUrl)

        return try {
            val optionsStatus = client.executeMethod(optionsMethod)

            if (!optionsMethod.isAllowed("SEARCH")) {
                client.exhaustResponse(optionsMethod.getResponseBodyAsStream())
                return RemoteOperationResult(false, optionsStatus, optionsMethod.responseHeaders)
            }

            val searchMethod =
                NcSearchMethod(
                    webDavUrl,
                    SearchInfo("NC", Namespace.XMLNS_NAMESPACE, searchQuery),
                    searchType,
                    client.userIdPlain,
                    timestamp,
                    limit,
                    filterOutFiles,
                    capability,
                    startDate,
                    endDate
                )

            try {
                val status = client.executeMethod(searchMethod)
                val isSuccess = status == HttpStatus.SC_MULTI_STATUS || status == HttpStatus.SC_OK

                if (isSuccess) {
                    val files =
                        WebDavFileUtils().readData(
                            searchMethod.getResponseBodyAsMultiStatus(),
                            client,
                            false,
                            true
                        )
                    RemoteOperationResult<MutableList<RemoteFile?>?>(true, status, searchMethod.responseHeaders)
                        .also { if (it.isSuccess) it.resultData = files }
                } else {
                    client.exhaustResponse(searchMethod.getResponseBodyAsStream())
                    RemoteOperationResult(false, status, searchMethod.responseHeaders)
                }
            } finally {
                searchMethod.releaseConnection()
            }
        } catch (e: Exception) {
            RemoteOperationResult(e)
        } finally {
            optionsMethod.releaseConnection()
        }
    }
}
