/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.common

object HTTPCodes {
    const val STATUS_OK = 200
    const val STATUS_UNAUTHORIZED = 401
    const val STATUS_FORBIDDEN = 403
    const val STATUS_NOT_FOUND = 404
    const val STATUS_REQUEST_TIMEOUT = 408
    const val STATUS_TOO_MANY_REQUESTS = 429
    const val STATUS_INTERNAL_SERVER_ERROR = 500
    const val STATUS_BAD_GATEWAY = 502
    const val STATUS_SERVICE_UNAVAILABLE = 503
    const val STATUS_GATEWAY_TIMEOUT = 504

    val RETRYABLE_HTTP_CODES =
        setOf(
            STATUS_REQUEST_TIMEOUT,
            STATUS_TOO_MANY_REQUESTS,
            STATUS_INTERNAL_SERVER_ERROR,
            STATUS_BAD_GATEWAY,
            STATUS_SERVICE_UNAVAILABLE,
            STATUS_GATEWAY_TIMEOUT
        )
}
