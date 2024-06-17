/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RenameFileRemoteOperationIT : AbstractIT() {
    @Test
    fun rename() {
        val filePath = createFile("file1")
        val oldRemotePath = "/file1.jpg"
        val newRemotePath = "/file2.png"
        assertTrue(
            UploadFileRemoteOperation(filePath, oldRemotePath, "image/jpg", RANDOM_MTIME)
                .execute(client)
                .isSuccess
        )

        assertTrue(
            RenameFileRemoteOperation("file1.jpg", oldRemotePath, "file2.png", false)
                .execute(client)
                .isSuccess
        )

        assertFalse(ExistenceCheckRemoteOperation(oldRemotePath, false).execute(client).isSuccess)
        assertTrue(ExistenceCheckRemoteOperation(newRemotePath, false).execute(client).isSuccess)
    }

    @Test
    fun renameToExistingFile() {
        val filePath = createFile("file1")
        val firstRemotePath = "/file1.jpg"
        val secondRemotePath = "/file2.png"
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

        val result =
            RenameFileRemoteOperation("file1.jpg", firstRemotePath, "file2.png", false)
                .execute(client)

        assertFalse(result.isSuccess)
        assertEquals(RemoteOperationResult.ResultCode.INVALID_OVERWRITE, result.code)
    }
}
