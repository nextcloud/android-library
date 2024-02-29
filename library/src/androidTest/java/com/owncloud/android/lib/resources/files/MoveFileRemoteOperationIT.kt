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
        var result =
            MoveFileRemoteOperation(firstRemotePath, secondRemotePath, false)
                .execute(client)

        assertFalse(result.isSuccess)
        assertEquals(RemoteOperationResult.ResultCode.INVALID_OVERWRITE, result.code)

        // then overwrite
        result =
            MoveFileRemoteOperation(firstRemotePath, secondRemotePath, true)
                .execute(client)

        assertTrue(result.isSuccess)

        assertTrue(
            ExistenceCheckRemoteOperation(secondRemotePath, false)
                .execute(client)
                .isSuccess
        )
    }
}
