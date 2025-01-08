/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020-2024 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2021 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-License-Identifier: MIT
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
                val searchProviders =
                    getServerResponse(
                        getMethod,
                        object : TypeToken<ServerResponse<ArrayList<SearchProvider>?>?>() {}
                    )?.ocs
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
