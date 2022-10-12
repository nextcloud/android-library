/*
 * <!--
 *   Nextcloud Android client application
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2020 Tobias Kaminsky
 *   Copyright (C) 2020 Nextcloud GmbH
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
import com.owncloud.android.lib.common.SearchProvider
import com.owncloud.android.lib.common.SearchProviders
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.ocs.ServerResponse
import com.owncloud.android.lib.resources.OCSRemoteOperation
import org.apache.commons.httpclient.HttpStatus

/**
 * Get all search providers for unified search
 */
@Suppress("TooGenericExceptionCaught")
class UnifiedSearchProvidersRemoteOperation : OCSRemoteOperation<SearchProviders>() {
    companion object {
        private val TAG = UnifiedSearchProvidersRemoteOperation::class.java.simpleName
        private const val ENDPOINT = "/ocs/v2.php/search/providers"
    }

    override fun run(client: NextcloudClient): RemoteOperationResult<SearchProviders> {
        var result: RemoteOperationResult<SearchProviders>
        var getMethod: GetMethod? = null
        try {
            getMethod = GetMethod(client.baseUri.toString() + ENDPOINT + JSON_FORMAT, true)

            // remote request
            getMethod.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE)
            val status = client.execute(getMethod)
            if (status == HttpStatus.SC_OK) {
                val searchProviders = getServerResponse(
                    getMethod,
                    object : TypeToken<ServerResponse<ArrayList<SearchProvider>?>?>() {}
                )
                    ?.ocs
                    ?.data
                    ?: ArrayList()

                val eTag = getMethod.getResponseHeader("ETag") ?: ""

                result = RemoteOperationResult(true, getMethod)
                result.resultData = SearchProviders(eTag, searchProviders)
            } else {
                result = RemoteOperationResult(false, getMethod)
            }
        } catch (e: Exception) {
            result = RemoteOperationResult(e)
            Log_OC.e(
                TAG,
                "Get all search provider failed: " + result.logMessage,
                result.exception
            )
        } finally {
            getMethod?.releaseConnection()
        }
        return result
    }
}
