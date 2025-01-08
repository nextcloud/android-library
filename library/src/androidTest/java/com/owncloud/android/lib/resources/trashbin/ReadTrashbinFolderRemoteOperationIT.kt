/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
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
            ).execute(client)
                .isSuccess
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
