/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Álvaro Brey <alvaro.brey@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class CreateLocalFileExceptionTest {
    private fun buildException(
        path: String,
        cause: Throwable = IOException()
    ): CreateLocalFileException = CreateLocalFileException(path, cause)

    @Test
    fun `causedByInvalidPath should be false if cause is not IOException`() {
        testInvalidPath("..::..://\\#<>", cause = Exception(), expected = false)
    }

    @Test
    fun `causedByInvalidPath should be false if path is valid`() {
        testInvalidPath("/path/too/foo.tar", expected = false)
        testInvalidPath("/path/too/foo", expected = false)
        testInvalidPath("/foo.tar", expected = false)
        testInvalidPath("/foo", expected = false)
        testInvalidPath("/", expected = false)
    }

    @Test
    fun `causedByInvalidPath should be true if path contains invalid chars and cause is IOException`() {
        testInvalidPath("")
        testInvalidPath("/path/to:to/foo.tar")
        testInvalidPath("/pa:th/to/foo.tar")

        testInvalidPath("/path/to/foo:tar")
        testInvalidPath("/path/to/foo\\tar")
        testInvalidPath("/path/to/foo<tar")
        testInvalidPath("/path/to/foo>tar")
        testInvalidPath("/path/to/foo*tar")
        testInvalidPath("/path/to/foo?tar")
        testInvalidPath("/path/to/foo|tar")
    }

    private fun testInvalidPath(
        path: String,
        cause: Throwable = IOException(),
        expected: Boolean = true
    ) {
        val exception = buildException(path, cause)
        assertEquals(
            "Wrong value for isCausedByInvalidPath, path=\"$path\"",
            expected,
            exception.isCausedByInvalidPath()
        )
    }
}
