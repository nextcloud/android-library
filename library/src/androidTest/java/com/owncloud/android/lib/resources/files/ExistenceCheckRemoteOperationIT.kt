/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import com.owncloud.android.AbstractIT
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExistenceCheckRemoteOperationIT : AbstractIT() {
    @Test
    fun checkFile() {
        val filePath = createFile("existenceCheckFile")
        val remotePath = "/existenceCheckFile.jpg"
        val notExistingRemotePath = "/notExistingCheckFile.jpg"
        assertTrue(
            UploadFileRemoteOperation(filePath, remotePath, "image/jpg", RANDOM_MTIME)
                .execute(client).isSuccess
        )

        var existenceResult =
            ExistenceCheckRemoteOperation(remotePath, false)
                .execute(client)
        assertTrue(existenceResult.isSuccess)

        existenceResult =
            ExistenceCheckRemoteOperation(notExistingRemotePath, false)
                .execute(client)
        assertFalse(existenceResult.isSuccess)
    }
}
