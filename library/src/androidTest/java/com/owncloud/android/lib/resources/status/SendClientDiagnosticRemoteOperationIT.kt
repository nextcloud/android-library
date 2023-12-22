/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2023 Tobias Kaminsky
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

package com.owncloud.android.lib.resources.status

import com.owncloud.android.AbstractIT
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@Suppress("Detekt.MagicNumber")
class SendClientDiagnosticRemoteOperationIT : AbstractIT() {
    @Test
    @Suppress("Detekt.MaxLineLength", "ktlint:standard:max-line-length")
    fun testJSON() {
        val problems =
            listOf(
                Problem("UploadResult.CREDENTIAL_ERROR", 2, 1700152062),
                Problem("UploadResult.FOLDER_ERROR", 3, 1400652062)
            )

        val sut =
            SendClientDiagnosticRemoteOperation(
                Problem(SendClientDiagnosticRemoteOperation.SYNC_CONFLICTS, 1, 1700652062),
                problems,
                Problem(SendClientDiagnosticRemoteOperation.VIRUS_DETECTED, 4, 1234234234),
                Problem(SendClientDiagnosticRemoteOperation.E2E_ERRORS, 2, 1700612062)
            )
        assertEquals(
            """{"sync_conflicts": {"count": 1, "oldest": 1700652062}, "problems": {"UploadResult.CREDENTIAL_ERROR": {"count": 2, "oldest": 1700152062}, "UploadResult.FOLDER_ERROR": {"count": 3, "oldest": 1400652062}}, "virus_detected": {"count": 4, "oldest": 1234234234}, "e2e_errors": {"count": 2, "oldest": 1700612062}}""",
            sut.generateJSON()
        )
    }

    @Test
    fun testEmptyJSON() {
        val sut =
            SendClientDiagnosticRemoteOperation(
                null,
                null,
                null,
                null
            )
        assertEquals(
            """{"sync_conflicts": {}, "problems": {}, "virus_detected": {}, "e2e_errors": {}}""",
            sut.generateJSON()
        )
    }

    @Test
    fun sendDiagnostic() {
        val problems =
            listOf(
                Problem("UploadResult.CREDENTIAL_ERROR", 2, 1700152062),
                Problem("UploadResult.FOLDER_ERROR", 3, 1400652062)
            )

        val sut =
            SendClientDiagnosticRemoteOperation(
                Problem(SendClientDiagnosticRemoteOperation.SYNC_CONFLICTS, 1, 1700652062),
                problems,
                null,
                Problem(SendClientDiagnosticRemoteOperation.E2E_ERRORS, 2, 1700612062)
            ).execute(nextcloudClient)
        assertTrue(sut.isSuccess) // we cannot check anything else
    }
}
