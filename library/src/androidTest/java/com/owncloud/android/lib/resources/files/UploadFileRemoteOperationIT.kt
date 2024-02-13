/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2022 Tobias Kaminsky
 *   Copyright (C) 2022 Nextcloud GmbH
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

import android.os.Build
import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.files.model.RemoteFile
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.TimeUnit

class UploadFileRemoteOperationIT : AbstractIT() {
    @Test
    fun creationTime() {
        val imageFile = getFile("imageFile.png")
        val creationDate = getCreationTimestamp(imageFile)
        assertNotNull(creationDate)
        assertTrue(creationDate!! > (System.currentTimeMillis() / MILLI_TO_SECOND) - TIME_OFFSET)
    }

    @Test
    fun upload() {
        // create file
        val filePath = createFile("text")
        val remotePath = "/test.md"

        val creationTimestamp = getCreationTimestamp(File(filePath))
        val sut =
            UploadFileRemoteOperation(
                filePath,
                remotePath,
                "text/markdown",
                "",
                RANDOM_MTIME,
                creationTimestamp,
                true
            )
        val uploadTimestamp = System.currentTimeMillis() / MILLI_TO_SECOND

        val uploadResult = sut.execute(client)
        assertTrue(uploadResult.isSuccess)

        // ReadFileRemoteOperation
        var result = ReadFileRemoteOperation(remotePath).execute(client)
        assertTrue(result.isSuccess)

        var remoteFile = result.data[0] as RemoteFile

        assertEquals(remotePath, remoteFile.remotePath)
        assertEquals(creationTimestamp, remoteFile.creationTimestamp)
        assertEquals(uploadResult.resultData, remoteFile.etag)
        assertTrue(
            uploadTimestamp - TIME_OFFSET < remoteFile.uploadTimestamp ||
                uploadTimestamp + TIME_OFFSET > remoteFile.uploadTimestamp
        )

        // ReadFolderRemoteOperation
        result = ReadFolderRemoteOperation(remotePath).execute(client)
        assertTrue(result.isSuccess)

        remoteFile = result.data[0] as RemoteFile

        assertEquals(remotePath, remoteFile.remotePath)
        assertEquals(creationTimestamp, remoteFile.creationTimestamp)
        assertTrue(
            uploadTimestamp - TIME_OFFSET < remoteFile.uploadTimestamp ||
                uploadTimestamp + TIME_OFFSET > remoteFile.uploadTimestamp
        )
    }

    private fun getCreationTimestamp(file: File): Long? {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return null
        } else {
            try {
                Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
                    .creationTime()
                    .to(TimeUnit.SECONDS)
            } catch (e: IOException) {
                Log_OC.e(
                    UploadFileRemoteOperation::class.java.simpleName,
                    "Failed to read creation timestamp for file: " + file.name
                )
                null
            }
        }
    }

    companion object {
        const val TIME_OFFSET = 10
    }
}
