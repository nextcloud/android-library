/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 ZetaTom <70907959+ZetaTom@users.noreply.github.com>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.operations

import android.net.Uri
import at.bitfire.dav4jvm.DavResource
import com.nextcloud.common.DavMethod
import com.nextcloud.common.DavResponse
import okhttp3.Headers.Companion.toHeaders
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.internal.http.StatusLine

class MkColMethod(httpUrl: HttpUrl) : DavMethod<DavResponse>(httpUrl) {
    private val headers = mutableMapOf<String, String>()

    override fun apply(
        client: OkHttpClient,
        httpUrl: HttpUrl,
        filesDavUri: Uri
    ): DavResponse {
        val result = DavResponse()

        DavResource(client, httpUrl).mkCol(
            xmlBody = null,
            headers = headers.toHeaders()
        ) { response: Response ->
            result.success = response.isSuccessful
            result.status = StatusLine.get(response)
            result.headers = response.headers
        }

        return result
    }

    fun addRequestHeader(
        key: String,
        value: String
    ) {
        headers[key] = value
    }
}
