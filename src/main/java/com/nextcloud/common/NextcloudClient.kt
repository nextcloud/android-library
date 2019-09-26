/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2019 Tobias Kaminsky
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
import com.owncloud.android.lib.common.operations.RemoteOperation
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.Request

class NextcloudClient(var baseUri: Uri, val context: Context) : OkHttpClient() {
    var client: OkHttpClient = Builder().cookieJar(CookieJar.NO_COOKIES).build()

    lateinit var credentials: String
    lateinit var userId: String
    var requestBuilder: Request.Builder = Request.Builder()


    fun execute(method: OkHttpMethodBase): Int {
        val temp = requestBuilder
                .header("Authorization", credentials)
                .header("User-Agent", "Test UserAgent") // TODO change me
                .header("http.protocol.single-cookie-header", "true")
                .url(method.buildQueryParameter())

        if (method.useOcsApiRequestHeader) {
            temp.header(RemoteOperation.OCS_API_HEADER, RemoteOperation.OCS_API_HEADER_VALUE)
        }

        val request = temp.build()

        method.response = client.newCall(request).execute()

        return method.response.code()
    }
}
