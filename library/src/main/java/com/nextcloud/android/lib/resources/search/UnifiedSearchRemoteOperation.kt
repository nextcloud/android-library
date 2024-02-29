/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022-2024 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.search

import com.google.gson.reflect.TypeToken
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.SearchResult
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.ServerResponse
import com.owncloud.android.lib.resources.OCSRemoteOperation
import org.apache.commons.httpclient.HttpStatus
import java.net.URLEncoder

/**
 * Get search result by a specific unified search provider
 */
@Suppress("TooGenericExceptionCaught")
class UnifiedSearchRemoteOperation(
    val provider: String,
    val query: String,
    val cursor: Int? = null,
    val limit: Int = 5
) :
    OCSRemoteOperation<SearchResult>() {
    companion object {
        private val TAG = UnifiedSearchRemoteOperation::class.java.simpleName
        private const val ENDPOINT = "/ocs/v2.php/search/providers/"
        private const val SEARCH = "/search"
        private const val TERM = "&term="
        private const val LIMIT = "&limit=%d"
        private const val CURSOR = "&cursor=%d"
    }

    override fun run(client: NextcloudClient): RemoteOperationResult<SearchResult> {
        if (query.isBlank()) {
            return RemoteOperationResult(
                IllegalArgumentException("Query may not be empty or blank!")
            )
        }

        var result: RemoteOperationResult<SearchResult>
        var getMethod: GetMethod? = null
        try {
            var uri =
                client.baseUri.toString() +
                    ENDPOINT +
                    provider +
                    SEARCH +
                    JSON_FORMAT +
                    TERM +
                    URLEncoder.encode(query, "UTF-8") +
                    LIMIT.format(limit)
            cursor?.let {
                uri += CURSOR.format(it)
            }
            getMethod =
                GetMethod(
                    uri,
                    true
                )

            // remote request
            getMethod.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE)
            val status = client.execute(getMethod)
            if (status == HttpStatus.SC_OK) {
                val searchProviders =
                    getServerResponse(
                        getMethod,
                        object : TypeToken<ServerResponse<SearchResult>?>() {}
                    )?.ocs?.data

                if (searchProviders != null) {
                    result = RemoteOperationResult(true, getMethod)
                    result.resultData = searchProviders
                } else {
                    result = RemoteOperationResult(false, getMethod)
                }
            } else {
                result = RemoteOperationResult(false, getMethod)
            }
        } catch (e: Exception) {
            result = RemoteOperationResult(e)
            Log_OC.e(
                TAG,
                "Search with " + query + " to " + provider + " failed:" + result.logMessage,
                result.exception
            )
        } finally {
            getMethod?.releaseConnection()
        }
        return result
    }
}
