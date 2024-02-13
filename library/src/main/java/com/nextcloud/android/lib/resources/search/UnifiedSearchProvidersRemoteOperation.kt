/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2020 Tobias Kaminsky
 *   Copyright (C) 2020 Nextcloud GmbH
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
