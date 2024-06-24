/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 ZetaTom <70907959+ZetaTom@users.noreply.github.com>
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.common

import android.net.Uri
import com.owncloud.android.lib.common.network.WebdavUtils
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

abstract class DavMethod<T>(private val httpUrl: HttpUrl) {
    fun execute(nextcloudClient: NextcloudClient): T {
        // register custom property
        WebdavUtils.registerCustomFactories()

        return apply(nextcloudClient.disabledRedirectClient(), httpUrl, nextcloudClient.filesDavUri)
    }

    abstract fun apply(
        client: OkHttpClient,
        httpUrl: HttpUrl,
        filesDavUri: Uri
    ): T
}
