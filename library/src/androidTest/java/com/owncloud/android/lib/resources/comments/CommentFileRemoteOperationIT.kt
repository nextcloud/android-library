/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.comments

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.files.ReadFileRemoteOperation
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation
import com.owncloud.android.lib.resources.files.model.RemoteFile
import junit.framework.Assert.assertTrue
import org.junit.Test

class CommentFileRemoteOperationIT : AbstractIT() {
    @Test
    fun comment() {
        val filePath: String = createFile("commentFile")
        val remotePath = "/commentFile.txt"
        assertTrue(
            UploadFileRemoteOperation(filePath, remotePath, "image/jpg", RANDOM_MTIME)
                .execute(client)
                .isSuccess
        )

        val readResult = ReadFileRemoteOperation(remotePath).execute(client)
        val remoteFile = readResult.data.get(0) as RemoteFile

        assertTrue(
            CommentFileRemoteOperation("test", remoteFile.localId)
                .execute(nextcloudClient)
                .isSuccess
        )

        assertTrue(
            MarkCommentsAsReadRemoteOperation(remoteFile.localId)
                .execute(client)
                .isSuccess
        )
    }
}
