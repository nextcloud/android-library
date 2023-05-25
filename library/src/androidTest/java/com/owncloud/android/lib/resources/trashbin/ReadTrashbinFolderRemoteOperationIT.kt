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
        assertTrue(
            UploadFileRemoteOperation(
                filePath,
                remotePath,
                "image/jpg",
                "1464818400"
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
