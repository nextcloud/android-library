/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 ZetaTom <70907959+zetatom@users.noreply.github.com>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.operations

import com.nextcloud.common.OkHttpMethodBase
import okhttp3.Request

/**
 * HTTP HEAD method that uses OkHttp with new NextcloudClient
 */
class HeadMethod(
    uri: String,
    useOcsApiRequestHeader: Boolean
) : OkHttpMethodBase(uri, useOcsApiRequestHeader) {
    override fun applyType(temp: Request.Builder) {
        temp.head()
    }
}
