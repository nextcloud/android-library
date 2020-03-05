/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2019 Tobias Kaminsky
 *   Copyright (C) 2019 Nextcloud GmbH
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

package com.nextcloud.common

import com.owncloud.android.lib.common.OwnCloudClientManagerFactory
import com.owncloud.android.lib.common.operations.RemoteOperation
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * Common base class for all new OkHttpMethods
 */
abstract class OkHttpMethodBase(var uri: String,
                                val useOcsApiRequestHeader: Boolean) {
    companion object {
        const val UNKNOWN_STATUS_CODE: Int = -1
    }
    
    private var response: Response? = null
    private var queryMap: Map<String, String> = HashMap()
    private val requestHeaders: MutableMap<String, String> = HashMap()
    private val requestBuilder: Request.Builder = Request.Builder()
    private var request: Request? = null

    fun OkHttpMethodBase() {
        requestHeaders.put("http.protocol.single-cookie-header", "true")
    }

    fun buildQueryParameter(): HttpUrl {
        val httpBuilder = HttpUrl.parse(uri)?.newBuilder() ?: throw IllegalStateException("Error")

        queryMap.forEach { (k, v) -> httpBuilder.addQueryParameter(k, v) }

        return httpBuilder.build()
    }

    /**
     * Set request headers completely replacing existing headers.
     * To clear request headers, call this method with empty list.
     *
     * @param headers List of header-value pairs
     */
    fun setRequestHeaders(vararg headers: Pair<String, String>) {
        requestHeaders.clear()
        requestHeaders.putAll(headers)
    }

    /**
     * Adds request header, overwriting any existing value.
     *
     * @param header HTTP request header name
     * @param value HTTP request header value
     */
    fun addRequestHeader(header: String, value: String) {
        requestHeaders.put(header, value)
    }

    fun setQueryString(params: Map<String, String>) {
        queryMap = params
    }

    fun getResponseBodyAsString(): String {
        return response?.body()?.string() ?: ""
    }

    fun releaseConnection() {
        response?.body()?.close()
    }

    fun getStatusCode(): Int {
        return response?.code() ?: UNKNOWN_STATUS_CODE
    }

    fun getStatusText(): String {
        return response?.message() ?: ""
    }

    fun getResponseHeaders(): Headers {
        return response?.headers() ?: Headers.Builder().build()
    }

    fun getResponseHeader(name: String): String? {
        return response?.header(name)
    }

    fun getRequestHeader(name: String): String? {
        return request?.header(name) ?: ""
    }

    /**
     * Execute operation using nextcloud client.
     *
     * @return HTTP return code or [UNKNOWN_STATUS_CODE] in case of network error.
     */
    fun execute(nextcloudClient: NextcloudClient): Int {
        val temp = requestBuilder.url(buildQueryParameter())

        requestHeaders.put("Authorization", nextcloudClient.credentials)
        requestHeaders.put("User-Agent", OwnCloudClientManagerFactory.getUserAgent())
        requestHeaders.forEach({ (name, value) -> temp.header(name, value) })

        if (useOcsApiRequestHeader) {
            temp.header(RemoteOperation.OCS_API_HEADER, RemoteOperation.OCS_API_HEADER_VALUE)
        }

        val request = temp.build()

        try {
            response = nextcloudClient.client.newCall(request).execute()
        } catch (ex: IOException) {
            return UNKNOWN_STATUS_CODE
        }

        return if (nextcloudClient.followRedirects) {
            nextcloudClient.followRedirection(this).getLastStatus()
        } else {
            response?.code() ?: UNKNOWN_STATUS_CODE
        }
    }
}
