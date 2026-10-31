/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.common

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Must be registered as network interceptor so that it runs for every hop of a redirect chain. OkHttp builds the
 * next hop from the request before the network interceptors ran, so headers added here never carry over to it.
 *
 * Library clients install it themselves; an [okhttp3.OkHttpClient] passed in by the caller has to carry it already.
 */
object CustomHeadersInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url
        val allowed = CustomHeaders.forDestination(url.scheme, url.host, url.port)
        if (allowed.isEmpty()) {
            return chain.proceed(request)
        }

        val builder = request.newBuilder()
        allowed.forEach { (name, value) -> builder.header(name, value) }
        return chain.proceed(builder.build())
    }
}
