/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.common

import okhttp3.Headers
import okhttp3.internal.http.StatusLine

/**
 * Encapsulates essential data returned as responses from various DAV calls.
 */
data class DavResponse(
    var success: Boolean = false,
    var status: StatusLine? = null,
    var headers: Headers? = null
) {
    /**
     * Return value of specified header.
     *
     * Simple helper to aid with nullability when called from Java.
     *
     * @param key name of header to get
     * @return value of header or `null` when header is not set
     */
    fun getHeader(key: String): String? = headers?.get(key)

    /**
     * Return value of status code.
     *
     * Simple helper to aid with nullability when called from Java.
     *
     * @return HTTP status code or `0` if not set.
     */
    fun getStatusCode(): Int = status?.code ?: 0
}
