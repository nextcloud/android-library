/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.extensions

import com.nextcloud.common.HTTPCodes
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import java.io.InterruptedIOException
import java.net.SocketException

@Suppress("ReturnCount")
fun RemoteOperationResult<Void>.isTransientFailure(): Boolean {
    if (isSuccess) return false

    exception?.let { ex ->
        return ex is SocketException ||
            ex is InterruptedIOException ||
            ex.cause is SocketException ||
            ex.cause is InterruptedIOException ||
            ex.message?.contains("transport", ignoreCase = true) == true
    }

    return httpCode in HTTPCodes.RETRYABLE_HTTP_CODES
}
