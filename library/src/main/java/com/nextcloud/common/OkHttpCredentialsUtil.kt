/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.common

import android.os.Build
import okhttp3.Credentials
import java.nio.charset.StandardCharsets

object OkHttpCredentialsUtil {
    /**
     * Builds basic OKHttp credentials string, using UTF_8 if available
     */
    @JvmStatic
    fun basic(
        username: String,
        password: String
    ): String =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ->
                Credentials.basic(username, password, StandardCharsets.UTF_8)
            else -> Credentials.basic(username, password)
        }
}
