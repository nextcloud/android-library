package com.nextcloud.common

import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.Response

@Suppress("MagicNumber")
class IPv4FallbackInterceptor(private val connectionPool: ConnectionPool) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val hostname = request.url.host

        return if (response.code in 500..599 && IPV6PreferringDNS.isIPV6(hostname)) {
            IPV6PreferringDNS.setIPVersionPreference(hostname, true)
            connectionPool.evictAll()
            chain.proceed(
                request
            )
        } else {
            response
        }
    }
}
