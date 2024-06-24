/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 ZetaTom <70907959+ZetaTom@users.noreply.github.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.common

import com.owncloud.android.lib.common.OwnCloudClientManagerFactory
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor for [okhttp3.Request]s.
 *
 * Adds user agent derived from [OwnCloudClientManagerFactory.getUserAgent] to every request.
 */
class UserAgentInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val userAgent = OwnCloudClientManagerFactory.getUserAgent()
        val request =
            chain.request()
                .newBuilder()
                .header("User-Agent", userAgent)
                .build()
        return chain.proceed(request)
    }
}
