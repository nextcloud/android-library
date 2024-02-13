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

package com.owncloud.android.lib.resources.trashbin

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.files.RemoveFileRemoteOperation
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ReadTrashbinFolderRemoteOperationIT : AbstractIT() {
    @Test
    fun trashbin() {
        assertTrue(EmptyTrashbinRemoteOperation().execute(nextcloudClient).isSuccess)

        val sut = ReadTrashbinFolderRemoteOperation("/")

        assertEquals(0, sut.execute(client).resultData.size)

        val fileName = "trashbinFile.txt"
        val filePath = createFile(fileName)
        val remotePath = "/$fileName"

        @Suppress("Detekt.MagicNumber")
        assertTrue(
            UploadFileRemoteOperation(
                filePath,
                remotePath,
                "image/jpg",
                1464818400
            )
                .execute(client).isSuccess
        )

        // delete file
        assertTrue(
            RemoveFileRemoteOperation(remotePath)
                .execute(client)
                .isSuccess
        )

        val result = sut.execute(client)

        assertEquals(1, result.resultData.size)
        assertEquals(fileName, result.resultData[0].fileName)
    }
}
