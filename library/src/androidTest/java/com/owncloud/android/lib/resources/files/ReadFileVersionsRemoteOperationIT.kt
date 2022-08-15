/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2021 Tobias Kaminsky
 * Copyright (C) 2021 Nextcloud GmbH
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.FileWriter

class ReadFileVersionsRemoteOperationIT : AbstractIT() {
    @Test
    fun listVersions() {
        val txtFile = getFile(ASSETS__TEXT_FILE_NAME)
        val filePath = "/test.md"

        var uploadResult = UploadFileRemoteOperation(
            txtFile.absolutePath,
            filePath,
            "txt/plain",
            (System.currentTimeMillis() / MILLI_TO_SECOND).toString()
        )
            .execute(client)

        assertTrue("Error uploading file $filePath: $uploadResult", uploadResult.isSuccess)

        var remoteFile = ReadFileRemoteOperation(filePath).execute(client).data[0] as RemoteFile

        var sutResult = ReadFileVersionsRemoteOperation(remoteFile.localId).execute(client)

        assertTrue(sutResult.isSuccess)
        assertEquals(0, sutResult.data.size)

        // modify file to have a version
        FileWriter(txtFile).apply {
            write("test\n")
            flush()
            close()
        }

        uploadResult = UploadFileRemoteOperation(
            txtFile.absolutePath,
            filePath,
            "txt/plain",
            (System.currentTimeMillis() / MILLI_TO_SECOND).toString()
        )
            .execute(client)

        assertTrue("Error uploading file $filePath: $uploadResult", uploadResult.isSuccess)

        remoteFile = ReadFileRemoteOperation(filePath).execute(client).data[0] as RemoteFile

        sutResult = ReadFileVersionsRemoteOperation(remoteFile.localId).execute(client)

        assertTrue(sutResult.isSuccess)
        assertEquals(1, sutResult.data.size)
    }
}
