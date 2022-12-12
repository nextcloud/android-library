/*
 * Nextcloud Android Library is available under MIT license
 *
 * @author Álvaro Brey Vilas
 * Copyright (C) 2022 Álvaro Brey Vilas
 * Copyright (C) 2022 Nextcloud GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.owncloud.android.lib.common.operations

import org.apache.commons.httpclient.Header
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteOperationResultTest {

    companion object {
        private val BEARER_HEADER = Header("www-authenticate", "Bearer realm=foo")
        private val BASIC_HEADER = Header("www-authenticate", "Basic foo")
        private val LOCATION_HEADER = Header("location", "nextcloud.localhost")
    }

    private fun genResultWithHeaders(headers: Array<Header>): RemoteOperationResult<Unit> =
        RemoteOperationResult<Unit>(
            false,
            401,
            "foo",
            arrayOf(
                Header("cache-control", "no cache"),
                Header("content-encoding", "gzip"),
                Header("server", "nextcloud.localhost")
            ) + headers
        )

    @Test
    fun test_multipleAuth_withLocation() {
        val sut = genResultWithHeaders(
            arrayOf(
                BEARER_HEADER,
                BASIC_HEADER,
                LOCATION_HEADER
            )
        )

        assertTrue(
            "Missing bearer auth header",
            sut.authenticateHeaders.any { it == BEARER_HEADER.value }
        )
        assertTrue(
            "Missing basic auth header",
            sut.authenticateHeaders.any { it == BASIC_HEADER.value }
        )
        assertEquals(
            "Wrong location header",
            null,
            sut.redirectedLocation
        )
    }

    @Test
    fun test_noLocation_singleAuth() {
        val sut = genResultWithHeaders(
            arrayOf(
                BEARER_HEADER
            )
        )

        assertEquals(
            "Wrong auth headers length",
            1,
            sut.authenticateHeaders.size
        )
        assertTrue(
            "Missing bearer auth header",
            sut.authenticateHeaders.any { it == BEARER_HEADER.value }
        )
        assertEquals(
            "Wrong location header",
            null,
            sut.redirectedLocation
        )
    }

    @Test
    fun test_noAuth_location() {
        val sut = genResultWithHeaders(
            arrayOf(
                LOCATION_HEADER
            )
        )

        assertEquals(
            "Wrong auth headers length",
            0,
            sut.authenticateHeaders.size
        )
        assertEquals(
            "Wrong location header",
            LOCATION_HEADER.value,
            sut.redirectedLocation
        )
    }
}
