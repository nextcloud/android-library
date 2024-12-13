/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias.kaminsky@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.operations

import okhttp3.Request
import okhttp3.RequestBody

/**
 * HTTP POST method that uses OkHttp with new NextcloudClient
 */
class Utf8PostMethod(
    uri: String,
    useOcsApiRequestHeader: Boolean,
    body: RequestBody?
) : PostMethod(uri, useOcsApiRequestHeader, body) {
    override fun applyType(temp: Request.Builder) {
        temp.addHeader("Content-Type", "charset=utf-8")
    }
}
