/*
 *  Nextcloud Android Library is available under MIT license
 *
 *  @author Álvaro Brey Vilas
 *  Copyright (C) 2022 Álvaro Brey Vilas
 *  Copyright (C) 2022 Nextcloud GmbH
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *  BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *  ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package com.nextcloud.common

import com.owncloud.android.lib.common.utils.Log_OC
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.net.ConnectException
import java.net.SocketTimeoutException

class IPv4FallbackInterceptor(private val connectionPool: ConnectionPool) : Interceptor {
    companion object {
        private const val TAG = "IPv4FallbackInterceptor"
        private val SERVER_ERROR_RANGE = 500..599
    }

    @Suppress("TooGenericExceptionCaught")
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val hostname = request.url.host

        return try {
            if (response.code in SERVER_ERROR_RANGE && DNSCache.isIPV6First(hostname)) {
                Log_OC.d(TAG, "Response error with IPv6, trying IPv4")
                retryWithIPv4(hostname, chain, request)
            } else {
                response
            }
        } catch (e: Exception) {
            if (DNSCache.isIPV6First(hostname) && (e is SocketTimeoutException || e is ConnectException)) {
                return retryWithIPv4(hostname, chain, request)
            }
            throw e
        }
    }

    private fun retryWithIPv4(
        hostname: String,
        chain: Interceptor.Chain,
        request: Request
    ): Response {
        Log_OC.d(TAG, "Error with IPv6, trying IPv4")
        DNSCache.setIPVersionPreference(hostname, true)
        connectionPool.evictAll()
        return try {
            chain.proceed(request)
        } catch (e: IllegalStateException) {
            Log_OC.w(TAG, e.stackTraceToString())
            chain.call().clone().execute()
        }
    }
}
