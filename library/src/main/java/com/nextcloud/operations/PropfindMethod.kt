/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.operations

import com.nextcloud.common.OkHttpMethodBase
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * HTTP PROPFIND method that uses OkHttp with new NextcloudClient
 */
class PropfindMethod(
    uri: String,
    useOcsApiRequestHeader: Boolean,
    private val requestBody: ByteArray,
    depth: Int
) : OkHttpMethodBase(uri, useOcsApiRequestHeader) {
    init {
        addRequestHeader(HEADER_DEPTH, depth.toString())
    }

    override fun applyType(temp: Request.Builder) {
        temp.method(METHOD_NAME, requestBody.toRequestBody(CONTENT_TYPE))
    }

    companion object {
        private const val METHOD_NAME = "PROPFIND"
        private const val HEADER_DEPTH = "Depth"
        private val CONTENT_TYPE = "text/xml; charset=UTF-8".toMediaTypeOrNull()
    }
}
