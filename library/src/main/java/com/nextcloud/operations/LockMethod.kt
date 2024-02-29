/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.operations

import com.nextcloud.common.OkHttpMethodBase
import okhttp3.Request

/**
 * HTTP LOCK method that uses OkHttp with new NextcloudClient
 */
class LockMethod(
    uri: String,
    useOcsApiRequestHeader: Boolean
) : OkHttpMethodBase(uri, useOcsApiRequestHeader) {
    override fun applyType(temp: Request.Builder) {
        temp.method("LOCK", null)
    }
}
