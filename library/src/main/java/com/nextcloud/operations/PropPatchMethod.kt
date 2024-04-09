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
import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.Response
import com.nextcloud.common.DavMethod
import com.nextcloud.common.DavResponse
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

class PropPatchMethod
    @JvmOverloads
    constructor(
        httpUrl: HttpUrl,
        private val setProperties: Map<Property.Name, String> = emptyMap(),
        private val removeProperties: List<Property.Name> = emptyList()
    ) : DavMethod<DavResponse>(httpUrl) {
        override fun apply(
            client: OkHttpClient,
            httpUrl: HttpUrl,
            filesDavUri: Uri
        ): DavResponse {
            val result = DavResponse()
            DavResource(
                client,
                httpUrl
            ).proppatch(setProperties, removeProperties) { response: Response, hrefRelation: Response.HrefRelation? ->
                result.success = response.isSuccess()
                response.status?.let { status ->
                    result.status = status
                }
            }

            return result
        }
    }
