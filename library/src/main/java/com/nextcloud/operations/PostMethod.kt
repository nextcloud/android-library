/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.operations

import com.nextcloud.common.OkHttpMethodBase
import okhttp3.Request
import okhttp3.RequestBody

/**
 * HTTP POST method that uses OkHttp with new NextcloudClient
 * UTF8 by default
 */
class PostMethod(
    uri: String,
    useOcsApiRequestHeader: Boolean,
    val body: RequestBody?
) : OkHttpMethodBase(uri, useOcsApiRequestHeader) {
    override fun applyType(temp: Request.Builder) {
        body?.let { temp.post(it) }
    }
}
