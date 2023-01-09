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
import android.net.Uri
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.OwnCloudClientFactory.DEFAULT_CONNECTION_TIMEOUT_LONG
import com.owncloud.android.lib.common.OwnCloudClientFactory.DEFAULT_DATA_TIMEOUT_LONG
import com.owncloud.android.lib.common.accounts.AccountUtils
import com.owncloud.android.lib.common.network.AdvancedX509TrustManager
import com.owncloud.android.lib.common.network.NetworkUtils
import com.owncloud.android.lib.common.network.RedirectionPath
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.httpclient.HttpStatus
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager

class NextcloudClient private constructor(
    val delegate: NextcloudUriDelegate,
    var credentials: String,
    val client: OkHttpClient
) : NextcloudUriProvider by delegate {
    var followRedirects = true

    constructor(
        baseUri: Uri,
        userId: String,
        credentials: String,
        client: OkHttpClient
    ) : this(NextcloudUriDelegate(baseUri, userId), credentials, client)

    var userId: String
        get() = delegate.userId!!
        set(value) {
            delegate.userId = value
        }

    companion object {
        @JvmStatic
        val TAG = NextcloudClient::class.java.simpleName

        private fun createDefaultClient(context: Context): OkHttpClient {
            val trustManager = AdvancedX509TrustManager(NetworkUtils.getKnownServersStore(context))

            val sslContext = NetworkUtils.getSSLContext()

            sslContext.init(null, arrayOf<TrustManager>(trustManager), null)
            val sslSocketFactory = sslContext.socketFactory

            return OkHttpClient.Builder()
                .cookieJar(CookieJar.NO_COOKIES)
                .connectTimeout(DEFAULT_CONNECTION_TIMEOUT_LONG, TimeUnit.MILLISECONDS)
                .readTimeout(DEFAULT_DATA_TIMEOUT_LONG, TimeUnit.MILLISECONDS)
                .callTimeout(DEFAULT_CONNECTION_TIMEOUT_LONG + DEFAULT_DATA_TIMEOUT_LONG, TimeUnit.MILLISECONDS)
                .sslSocketFactory(sslSocketFactory, trustManager)
                .hostnameVerifier { _: String?, _: SSLSession? -> true }
                .fastFallback(true)
                .build()
        }
    }

    constructor(
        baseUri: Uri,
        userId: String,
        credentials: String,
        context: Context
    ) : this(baseUri, userId, credentials, createDefaultClient(context))

    @Suppress("TooGenericExceptionCaught")
    fun <T> execute(remoteOperation: RemoteOperation<T>): RemoteOperationResult<T> {
        return try {
            remoteOperation.run(this)
        } catch (ex: Exception) {
            RemoteOperationResult(ex)
        }
    }

    @Throws(IOException::class)
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

    @Throws(IOException::class)
    fun followRedirection(method: OkHttpMethodBase): RedirectionPath {
        var redirectionsCount = 0
        var status = method.getStatusCode()
        val result = RedirectionPath(status, OwnCloudClient.MAX_REDIRECTIONS_COUNT)

        val statusIsRedirection = status == HttpStatus.SC_MOVED_PERMANENTLY ||
            status == HttpStatus.SC_MOVED_TEMPORARILY ||
            status == HttpStatus.SC_TEMPORARY_REDIRECT
        while (redirectionsCount < OwnCloudClient.MAX_REDIRECTIONS_COUNT && statusIsRedirection) {
            val location = method.getResponseHeader("Location")
                ?: method.getResponseHeader("location")

            if (location != null) {
                Log_OC.d(TAG, "Location to redirect: $location")
                result.addLocation(location)
                // Release the connection to avoid reach the max number of connections per host
                // due to it will be set a different url
                method.releaseConnection()
                method.uri = location
                val destination = method.getRequestHeader("Destination")
                    ?: method.getRequestHeader("destination")

                if (destination != null) {
                    setRedirectedDestinationHeader(method, location, destination)
                }

                status = method.execute(this)
                result.addStatus(status)
                redirectionsCount++
            } else {
                Log_OC.d(TAG, "No location to redirect!")
                status = HttpStatus.SC_NOT_FOUND
                result.addStatus(status)
            }
        }
        return result
    }

    private fun setRedirectedDestinationHeader(
        method: OkHttpMethodBase,
        location: String,
        destination: String
    ) {
        val suffixIndex = location.lastIndexOf(AccountUtils.WEBDAV_PATH_9_0)
        val redirectionBase = location.substring(0, suffixIndex)
        val destinationPath = destination.substring(baseUri.toString().length)
        val redirectedDestination = redirectionBase + destinationPath

        if (method.getRequestHeader("Destination").isNullOrEmpty()) {
            method.addRequestHeader("destination", redirectedDestination)
        } else {
            method.addRequestHeader("Destination", redirectedDestination)
        }
    }

    fun getUserIdEncoded(): String {
        return delegate.userIdEncoded!!
    }

    fun getUserIdPlain(): String {
        return delegate.userId!!
    }
}
