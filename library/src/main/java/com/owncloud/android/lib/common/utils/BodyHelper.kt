/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.common.utils

import okhttp3.ResponseBody

object BodyHelper {
    private const val MAX_RESPONSE_BODY_SIZE: Long = 1_048_576
    private const val MB = 1024
    private const val BUFFER = 8192

    @Suppress("NestedBlockDepth")
    fun parseResponse(body: ResponseBody): String {
        body.byteStream().use { input ->
            val buffer = ByteArray(BUFFER)
            val output = StringBuilder()
            var bytesRead: Int
            var totalRead = 0L
            var wasTruncated = false

            while (input.read(buffer).also { bytesRead = it } != -1) {
                if (totalRead + bytesRead > MAX_RESPONSE_BODY_SIZE) {
                    val remaining = (MAX_RESPONSE_BODY_SIZE - totalRead).toInt()
                    if (remaining > 0) {
                        output.append(String(buffer, 0, remaining, Charsets.UTF_8))
                    }
                    wasTruncated = true
                    break
                } else {
                    output.append(String(buffer, 0, bytesRead, Charsets.UTF_8))
                    totalRead += bytesRead
                }
            }

            if (wasTruncated) {
                output.append("\n...[truncated output; showing first ${MAX_RESPONSE_BODY_SIZE / MB} KB]...")
            }

            return output.toString()
        }
    }
}
