/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH
 * SPDX-FileCopyrightText: 2024 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.users

import com.owncloud.android.AbstractIT
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.net.HttpURLConnection

class CheckRemoteWipeRemoteOperationIT : AbstractIT() {
    @Test
    fun checkRemoteWipe() {
        val sut = ConvertAppTokenRemoteOperation()
        val resultAppToken = sut.execute(nextcloudClient)
        assertTrue(resultAppToken.isSuccess)

        val newPassword = resultAppToken.resultData

        val result = CheckRemoteWipeRemoteOperation(newPassword).execute(nextcloudClient)

        // if no remote wipe, then 404 / false
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.httpCode)
        assertFalse(result.isSuccess)
    }
}
