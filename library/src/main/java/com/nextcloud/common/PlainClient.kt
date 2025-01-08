/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Elv1zz <elv1zz.git@gmail.com>
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2020-2021 Tobias Kaminsky
 * SPDX-FileCopyrightText: 2020 Chris Narkiewicz <hello@ezaquarii.com>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.common

import android.content.Context
import android.text.TextUtils
import com.owncloud.android.lib.common.OwnCloudClientFactory.DEFAULT_DATA_TIMEOUT_LONG
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory
import com.owncloud.android.lib.common.network.AdvancedX509KeyManager
import com.owncloud.android.lib.common.network.AdvancedX509TrustManager
import com.owncloud.android.lib.common.network.NetworkUtils
import com.owncloud.android.lib.common.utils.Log_OC
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager

class PlainClient(
    context: Context
) {
    var followRedirects = true
    var client: OkHttpClient = createDefaultClient(context)

    companion object {
        @JvmStatic
        val TAG = PlainClient::class.java.simpleName

        private fun createDefaultClient(context: Context): OkHttpClient {
            val trustManager = AdvancedX509TrustManager(NetworkUtils.getKnownServersStore(context))
            val keyManager = AdvancedX509KeyManager(context)

            val sslContext = NetworkUtils.getSSLContext()

            sslContext.init(arrayOf(keyManager), arrayOf<TrustManager>(trustManager), null)
            val sslSocketFactory = sslContext.socketFactory

            var proxy: Proxy? = null

            val proxyHost = OwnCloudClientManagerFactory.getProxyHost()
            val proxyPort = OwnCloudClientManagerFactory.getProxyPort()

            if (!TextUtils.isEmpty(proxyHost) && proxyPort > 0) {
                proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyHost, proxyPort))
                Log_OC.d(this, "Proxy settings: $proxyHost:$proxyPort")
            }

            return OkHttpClient
                .Builder()
                .cookieJar(CookieJar.NO_COOKIES)
                .callTimeout(DEFAULT_DATA_TIMEOUT_LONG, TimeUnit.MILLISECONDS)
                .sslSocketFactory(sslSocketFactory, trustManager)
                .hostnameVerifier { _: String?, _: SSLSession? -> true }
                .fastFallback(true)
                .proxy(proxy)
                .build()
        }
    }

    @Throws(Exception::class)
    fun execute(method: OkHttpMethodBase): Int = method.execute(this)

    internal fun execute(request: Request): ResponseOrError =
        try {
            val response = client.newCall(request).execute()
            ResponseOrError(response)
        } catch (ex: IOException) {
            ResponseOrError(ex)
        }
}
