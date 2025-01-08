/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019-2022 Tobias Kaminsky
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2020 Chris Narkiewicz <hello@ezaquarii.com>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.common

import com.owncloud.android.lib.common.OwnCloudClientManagerFactory
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.utils.Log_OC
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.HttpURLConnection

/**
 * Common base class for all new OkHttpMethods
 */
@Suppress("TooManyFunctions")
abstract class OkHttpMethodBase(
    var uri: String,
    private val useOcsApiRequestHeader: Boolean
) {
    companion object {
        const val UNKNOWN_STATUS_CODE: Int = -1
        const val USER_AGENT = "User-Agent"
        const val AUTHORIZATION = "Authorization"
    }

    private var response: Response? = null
    private var queryMap: Map<String, String> = HashMap()
    private val requestHeaders: MutableMap<String, String> = HashMap()
    private val requestBuilder: Request.Builder = Request.Builder()
    private var request: Request? = null

    init {
        requestHeaders["http.protocol.single-cookie-header"] = "true"
    }

    @Throws(IllegalStateException::class)
    private fun buildQueryParameter(): HttpUrl {
        val httpBuilder =
            uri.toHttpUrlOrNull()?.newBuilder() ?: throw IllegalStateException("Error")

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
    fun addRequestHeader(
        header: String,
        value: String
    ) {
        requestHeaders[header] = value
    }

    fun setQueryString(params: Map<String, String>) {
        queryMap = params
    }

    fun getResponseBodyAsString(): String = response?.body?.string() ?: ""

    fun getResponseContentLength(): Long = response?.body?.contentLength() ?: -1

    fun releaseConnection() {
        response?.body?.close()
    }

    fun getStatusCode(): Int = response?.code ?: UNKNOWN_STATUS_CODE

    fun getStatusText(): String = response?.message ?: ""

    fun getResponseHeaders(): Headers = response?.headers ?: Headers.Builder().build()

    fun getResponseHeader(name: String): String? = response?.header(name)

    fun getRequestHeader(name: String): String? = request?.header(name)

    /**
     * Execute operation using nextcloud client.
     *
     * @return HTTP return code or [UNKNOWN_STATUS_CODE] in case of network error.
     */
    fun execute(nextcloudClient: NextcloudClient): Int {
        val temp = requestBuilder.url(buildQueryParameter())

        requestHeaders[AUTHORIZATION] = nextcloudClient.credentials
        requestHeaders[USER_AGENT] = OwnCloudClientManagerFactory.getUserAgent()
        requestHeaders.forEach { (name, value) -> temp.header(name, value) }

        if (useOcsApiRequestHeader) {
            temp.header(RemoteOperation.OCS_API_HEADER, RemoteOperation.OCS_API_HEADER_VALUE)
        }

        applyType(temp)

        val request = temp.build()

        try {
            response = nextcloudClient.client.newCall(request).execute()
        } catch (ex: IOException) {
            return UNKNOWN_STATUS_CODE
        }

        return if (nextcloudClient.followRedirects) {
            nextcloudClient.followRedirection(this).lastStatus
        } else {
            response?.code ?: UNKNOWN_STATUS_CODE
        }
    }

    fun execute(client: PlainClient): Int {
        val temp = requestBuilder.url(buildQueryParameter())

        requestHeaders[USER_AGENT] = OwnCloudClientManagerFactory.getUserAgent()
        requestHeaders.forEach { (name, value) -> temp.header(name, value) }

        applyType(temp)

        val request = temp.build()

        try {
            response = client.client.newCall(request).execute()
        } catch (ex: IOException) {
            Log_OC.e(this, "Error executing method", ex)
        }

        return response?.code ?: UNKNOWN_STATUS_CODE
    }

    abstract fun applyType(temp: Request.Builder)

    fun isSuccess(): Boolean = getStatusCode() == HttpURLConnection.HTTP_OK
}
