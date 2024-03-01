/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.operations

import com.nextcloud.common.OkHttpMethodBase
import okhttp3.Request
import okhttp3.RequestBody

/**
 * HTTP PUT method that uses OkHttp with new NextcloudClient
 */
class PutMethod(
    uri: String,
    useOcsApiRequestHeader: Boolean,
    val body: RequestBody? = null
) : OkHttpMethodBase(uri, useOcsApiRequestHeader) {
    override fun applyType(temp: Request.Builder) {
        body?.let {
            temp.put(it)
        }
    }
}
