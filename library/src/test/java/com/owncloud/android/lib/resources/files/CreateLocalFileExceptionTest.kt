/* Nextcloud Android Library is available under MIT license
 *
 *   @author Álvaro Brey
 *   Copyright (C) 2023 Álvaro Brey
 *   Copyright (C) 2023 Nextcloud GmbH
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 */
package com.owncloud.android.lib.resources.files

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class CreateLocalFileExceptionTest {

    private fun buildException(path: String, cause: Throwable = IOException()): CreateLocalFileException {
        return CreateLocalFileException(path, cause)
    }

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

    private fun testInvalidPath(path: String, cause: Throwable = IOException(), expected: Boolean = true) {
        val exception = buildException(path, cause)
        assertEquals(
            "Wrong value for isCausedByInvalidPath, path=\"$path\"",
            expected,
            exception.isCausedByInvalidPath()
        )
    }
}
