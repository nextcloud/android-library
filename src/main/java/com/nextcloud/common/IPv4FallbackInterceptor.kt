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
            if (response.code in SERVER_ERROR_RANGE && DNSCache.isIPV6(hostname)) {
                Log_OC.d(TAG, "Response error with IPv6, trying IPv4")
                retryWithIPv4(hostname, chain, request)
            } else {
                response
            }
        } catch (e: Exception) {
            if (DNSCache.isIPV6(hostname) && (e is SocketTimeoutException || e is ConnectException)) {
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
        return chain.proceed(request)
    }
}
