/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import com.owncloud.android.AbstractIT
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DownloadFileRemoteOperationIT : AbstractIT() {
    @Test
    fun download() {
        val filePath = createFile("download")
        val remotePath = "/download.jpg"
        assertTrue(
            @Suppress("Detekt.MagicNumber")
            UploadFileRemoteOperation(filePath, remotePath, "image/jpg", 1464818400)
                .execute(client)
                .isSuccess
        )

        assertTrue(
            DownloadFileRemoteOperation(remotePath, context.externalCacheDir?.absolutePath)
                .execute(client)
                .isSuccess
        )

        val oldFile = File(filePath)
        val newFile = File(context.externalCacheDir?.absolutePath + remotePath)
        assertSame(oldFile.length(), newFile.length())
    }
}
