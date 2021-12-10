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

class ReadFolderRemoteOperationIT : AbstractIT() {
    @Test
    fun readRemoteFolderWithContent() {
        val remotePath = "/test/"

        assertTrue(CreateFolderRemoteOperation(remotePath, true).execute(client).isSuccess)

        // create file
        val filePath = createFile("text")
        assertTrue(
            UploadFileRemoteOperation(filePath, remotePath + "1.txt", "text/markdown", RANDOM_MTIME)
                .execute(client).isSuccess
        )

        val result = ReadFolderRemoteOperation(remotePath).execute(client)

        assertTrue(result.isSuccess)
        assertEquals(2, result.data.size)
        assertEquals(remotePath, (result.data[0] as RemoteFile).remotePath)
        assertEquals(remotePath + "1.txt", (result.data[1] as RemoteFile).remotePath)
    }
}
