/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2021 Tobias Kaminsky
 *   Copyright (C) 2021 Nextcloud GmbH
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

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MoveFileRemoteOperationIT : AbstractIT() {
    @Test
    fun move() {
        val filePath = createFile("move1")
        val oldRemotePath = "/move1.jpg"
        val newRemotePath = "/move2.png"
        assertTrue(
            UploadFileRemoteOperation(filePath, oldRemotePath, "image/jpg", RANDOM_MTIME)
                .execute(client)
                .isSuccess
        )

        assertTrue(
            MoveFileRemoteOperation(oldRemotePath, newRemotePath, false)
                .execute(client)
                .isSuccess
        )

        assertFalse(
            ExistenceCheckRemoteOperation(oldRemotePath, false)
                .execute(client)
                .isSuccess
        )
        assertTrue(
            ExistenceCheckRemoteOperation(newRemotePath, false)
                .execute(client)
                .isSuccess
        )
    }

    @Test
    fun moveToExistingFile() {
        val filePath = createFile("overwrite1")
        val firstRemotePath = "/overwrite1.jpg"
        val secondRemotePath = "/overwrite2.png"
        assertTrue(
            UploadFileRemoteOperation(filePath, firstRemotePath, "image/jpg", RANDOM_MTIME)
                .execute(client)
                .isSuccess
        )

        assertTrue(
            UploadFileRemoteOperation(filePath, secondRemotePath, "image/jpg", RANDOM_MTIME)
                .execute(client)
                .isSuccess
        )

        // first not try to overwrite
        var result = MoveFileRemoteOperation(firstRemotePath, secondRemotePath, false)
            .execute(client)

        assertFalse(result.isSuccess)
        assertEquals(RemoteOperationResult.ResultCode.INVALID_OVERWRITE, result.code)

        // then overwrite
        result = MoveFileRemoteOperation(firstRemotePath, secondRemotePath, true)
            .execute(client)

        assertTrue(result.isSuccess)

        assertTrue(
            ExistenceCheckRemoteOperation(secondRemotePath, false)
                .execute(client)
                .isSuccess
        )
    }
}
