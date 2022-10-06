/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Your Name <your@email.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.shares

import com.owncloud.android.AbstractIT
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class GetShareesRemoteOperationIT : AbstractIT() {
    companion object {
        const val PER_PAGE = 50
    }

    @Test
    fun getSharees() {
        val sut = GetShareesRemoteOperation("admin", 1, PER_PAGE).execute(client)
        assertTrue(sut.isSuccess)
        assertEquals(2, sut.resultData?.size)
    }
}
