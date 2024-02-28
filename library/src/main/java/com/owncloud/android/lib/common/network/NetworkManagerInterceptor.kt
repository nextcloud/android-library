/*
 * Nextcloud Android client application
 *
 * @author Alper Ozturk
 * Copyright (C) 2024 Alper Ozturk
 * Copyright (C) 2024 Nextcloud GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.owncloud.android.lib.common.network

import com.owncloud.android.lib.common.utils.Log_OC
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset

class NetworkManagerInterceptor : Interceptor {

    private val tag = "NetworkManagerInterceptor"

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        Log_OC.d(tag, "Sending request: ${request.method} ${request.url}")

        logRequestBody(request)

        val response = chain.proceed(request)

        Log_OC.d(tag, "Received response: ${response.code} ${response.message} ${response.request.url}")

        logResponseBody(response)

        return response
    }

    private fun logRequestBody(request: Request) {
        val requestBody = request.body ?: return
        val buffer = Buffer()
        requestBody.writeTo(buffer)
        val charset: Charset =
            requestBody.contentType()?.charset(Charset.forName("UTF-8")) ?: Charset.forName("UTF-8")

        Log_OC.d(tag, buffer.readString(charset))
    }

    private fun logResponseBody(response: Response) {
        val responseBody = response.body
        val source = responseBody.source()
        source.request(Long.MAX_VALUE)
        val buffer = source.buffer
        val charset: Charset = responseBody.contentType()?.charset(Charset.forName("UTF-8"))
            ?: Charset.forName("UTF-8")

        Log_OC.d(tag, buffer.clone().readString(charset))
    }
}
