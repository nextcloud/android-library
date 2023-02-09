/*
 *
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2023 Tobias Kaminsky
 * Copyright (C) 2023 Nextcloud GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package com.owncloud.android.lib.resources.files.webdav

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode
import com.owncloud.android.lib.resources.files.ChunkedFileUploadRemoteOperation
import junit.framework.TestCase
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ChunkedFileUploadRemoteOperationIT : AbstractIT() {
    @Test
    fun upload() {
        // create file
        val filePath = createFile("chunkedFile.txt", BIG_FILE_ITERATION)
        val remotePath = "/bigFile.md"

        val sut = ChunkedFileUploadRemoteOperation(
            filePath,
            remotePath,
            "text/markdown",
            "",
            RANDOM_MTIME,
            System.currentTimeMillis() / MILLI_TO_SECOND,
            true,
            true
        )

        val uploadResult = sut.execute(client)
        assertTrue(uploadResult.isSuccess)
    }

    @Test
    fun cancel() {
        // create file
        val filePath = createFile("chunkedFile.txt", BIG_FILE_ITERATION)
        val remotePath = "/cancelFile.md"

        val sut = ChunkedFileUploadRemoteOperation(
            filePath,
            remotePath,
            "text/markdown",
            "",
            RANDOM_MTIME,
            System.currentTimeMillis() / MILLI_TO_SECOND,
            false,
            true
        )

        var uploadResult: RemoteOperationResult<String>? = null
        Thread {
            uploadResult = sut.execute(client)
        }.start()

        shortSleep()
        sut.cancel(ResultCode.CANCELLED)

        for (i in 1..MAX_TRIES) {
            shortSleep()

            if (uploadResult != null) {
                break
            }
        }

        assertNotNull(uploadResult)
        TestCase.assertFalse(uploadResult?.isSuccess == true)
        TestCase.assertSame(ResultCode.CANCELLED, uploadResult?.code)
    }

    companion object {
        val BIG_FILE_ITERATION = 500000
        val MAX_TRIES = 30
    }
}
