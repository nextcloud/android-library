/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro.brey@nextcloud.com>
 * SPDX-License-Identifier: MIT
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
        val sut =
            genResultWithHeaders(
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
        val sut =
            genResultWithHeaders(
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
        val sut =
            genResultWithHeaders(
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
