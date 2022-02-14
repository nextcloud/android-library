/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2022 Tobias Kaminsky
 * Copyright (C) 2022 Nextcloud GmbH
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 */
package com.owncloud.android.lib.resources.files

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.files.model.RemoteFile
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class UploadFileRemoteOperationIT : AbstractIT() {

    @Test
    fun creationTime() {
        val sut = UploadFileRemoteOperation()

        val imageFile = getFile("imageFile.png")
        val creationDate = sut.getCreationTimestamp(imageFile) ?: 0

        assertTrue(creationDate > (System.currentTimeMillis() / MILLI_TO_SECOND) - TIME_OFFSET)
    }

    @Test
    fun upload() {
        // create file
        val filePath = createFile("text")
        val remotePath = "/test.md"

        val sut = UploadFileRemoteOperation(filePath, remotePath, "text/markdown", RANDOM_MTIME)
        val creationTimestamp = sut.getCreationTimestamp(File(filePath))
        val uploadTimestamp = System.currentTimeMillis() / MILLI_TO_SECOND

        val uploadResult = sut.execute(client)
        assertTrue(uploadResult.isSuccess)

        // ReadFileRemoteOperation
        var result = ReadFileRemoteOperation(remotePath).execute(client)
        assertTrue(result.isSuccess)

        var remoteFile = result.data[0] as RemoteFile

        assertEquals(remotePath, remoteFile.remotePath)
        assertEquals(creationTimestamp, remoteFile.creationTimestamp)
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

    companion object {
        const val TIME_OFFSET = 10
        const val MILLI_TO_SECOND = 1000
    }
}
