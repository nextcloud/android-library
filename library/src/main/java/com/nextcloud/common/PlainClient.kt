/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2019 Tobias Kaminsky
 *   Copyright (C) 2020 Chris Narkiewicz <hello@ezaquarii.com>
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

class PlainClient(context: Context) {
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

            return OkHttpClient.Builder()
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
    fun execute(method: OkHttpMethodBase): Int {
        return method.execute(this)
    }

    internal fun execute(request: Request): ResponseOrError {
        return try {
            val response = client.newCall(request).execute()
            ResponseOrError(response)
        } catch (ex: IOException) {
            ResponseOrError(ex)
        }
    }
}
