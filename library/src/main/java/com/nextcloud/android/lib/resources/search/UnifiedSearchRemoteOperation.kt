/*
 * <!--
 *   Nextcloud Android client application
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2019 Tobias Kaminsky
 *   Copyright (C) 2019 Nextcloud GmbH
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 * -->
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
            var uri = client.baseUri.toString() +
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
            getMethod = GetMethod(
                uri,
                true
            )

            // remote request
            getMethod.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE)
            val status = client.execute(getMethod)
            if (status == HttpStatus.SC_OK) {
                val searchProviders = getServerResponse(
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
