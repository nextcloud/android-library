/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.utils

import com.owncloud.android.lib.common.utils.BodyHelper
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test

class BodyHelperTests {
    companion object {
        private const val MAX_RESPONSE_BODY_SIZE = 1_048_576
        private const val ONE_HUNDRED = 100
    }

    @Test
    fun testParseResponseWhenBodyIsSmallerThanLimitShouldReturnFullContent() {
        val content = "Hello World"
        val body = content.toResponseBody("text/plain".toMediaTypeOrNull())

        val result = BodyHelper.parseResponse(body)

        assertEquals(content, result)
    }

    @Test
    fun testParseResponseWhenBodyIsExactlyAtLimitShouldReturnFullContent() {
        val content = "a".repeat(MAX_RESPONSE_BODY_SIZE)
        val body = content.toResponseBody("text/plain".toMediaTypeOrNull())

        val result = BodyHelper.parseResponse(body)

        assertEquals(content, result)
    }

    @Test
    fun testParseResponseWhenBodyIsLargerThanLimitShouldReturnTruncatedContentWithNotice() {
        val content = "b".repeat(MAX_RESPONSE_BODY_SIZE + ONE_HUNDRED)
        val body = content.toResponseBody("text/plain".toMediaTypeOrNull())

        val result = BodyHelper.parseResponse(body)

        val expectedPrefix = "b".repeat(MAX_RESPONSE_BODY_SIZE)
        val expectedSuffix = "\n...[truncated output; showing first 1024 KB]..."

        assert(result.startsWith(expectedPrefix.take(ONE_HUNDRED)))
        assert(result.endsWith(expectedSuffix))
    }

    @Test
    fun testParseResponseWhenBodyIsEmptyShouldReturnEmptyString() {
        val body = "".toResponseBody("text/plain".toMediaTypeOrNull())
        val result = BodyHelper.parseResponse(body)
        assertEquals("", result)
    }
}
