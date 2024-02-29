/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020-2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android

import com.nextcloud.common.PlainClient
import com.nextcloud.operations.GetMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.net.HttpURLConnection

class PlainClientIT : AbstractIT() {
    @Test
    fun test204Success() {
        val sut = PlainClient(context)
        val getMethod = GetMethod("$url/index.php/204", false)

        val status = getMethod.execute(sut)

        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, status)
    }

    @Test
    fun test204Error() {
        val sut = PlainClient(context)
        val getMethod = GetMethod("$url/index.php", false)

        val status = getMethod.execute(sut)

        assertNotEquals(HttpURLConnection.HTTP_NO_CONTENT, status)
    }
}
