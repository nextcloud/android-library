/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import com.nextcloud.test.RandomStringGenerator
import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.files.model.RemoteFile
import com.owncloud.android.lib.resources.status.NextcloudVersion
import com.owncloud.android.lib.resources.tags.CreateTagRemoteOperation
import com.owncloud.android.lib.resources.tags.GetTagsRemoteOperation
import com.owncloud.android.lib.resources.tags.PutTagRemoteOperation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadFolderRemoteOperationIT : AbstractIT() {
    companion object {
        const val TAG_LENGTH = 10
    }

    @Test
    fun readRemoteFolderWithContent() {
        val remotePath = "/test/"

        assertTrue(CreateFolderRemoteOperation(remotePath, true).execute(client).isSuccess)

        // create file
        val filePath = createFile("text")
        assertTrue(
            UploadFileRemoteOperation(filePath, remotePath + "1.txt", "text/markdown", RANDOM_MTIME)
                .execute(client)
                .isSuccess
        )

        var result = ReadFolderRemoteOperation(remotePath).execute(client)

        assertTrue(result.isSuccess)
        assertEquals(2, result.data.size)

        // tag testing only on NC27+
        testOnlyOnServer(NextcloudVersion.nextcloud_27)

        // Folder
        var remoteFolder = result.data[0] as RemoteFile
        assertEquals(remotePath, remoteFolder.remotePath)
        assertEquals(0, remoteFolder.tags?.size)

        // File
        var remoteFile = result.data[1] as RemoteFile
        assertEquals(remotePath + "1.txt", remoteFile.remotePath)
        assertEquals(0, remoteFile.tags?.size)

        // create tag
        val tag1 = "a" + RandomStringGenerator.make(TAG_LENGTH)
        val tag2 = "b" + RandomStringGenerator.make(TAG_LENGTH)
        assertTrue(CreateTagRemoteOperation(tag1).execute(nextcloudClient).isSuccess)
        assertTrue(CreateTagRemoteOperation(tag2).execute(nextcloudClient).isSuccess)

        // list tags
        val tags = GetTagsRemoteOperation().execute(client).resultData

        // add tag
        assertTrue(
            PutTagRemoteOperation(
                tags[0].id,
                remoteFile.localId
            ).execute(nextcloudClient).isSuccess
        )
        assertTrue(
            PutTagRemoteOperation(
                tags[1].id,
                remoteFile.localId
            ).execute(nextcloudClient).isSuccess
        )

        // check again
        result = ReadFolderRemoteOperation(remotePath).execute(client)

        assertTrue(result.isSuccess)
        assertEquals(2, result.data.size)

        // Folder
        remoteFolder = result.data[0] as RemoteFile
        assertEquals(remotePath, remoteFolder.remotePath)
        assertEquals(0, remoteFolder.tags?.size)

        // File
        remoteFile = result.data[1] as RemoteFile
        assertEquals(remotePath + "1.txt", remoteFile.remotePath)
        assertEquals(2, remoteFile.tags?.size)

        remoteFile.tags?.sortBy { it?.name }
        assertEquals(tag1, remoteFile.tags?.get(0))
        assertEquals(tag2, remoteFile.tags?.get(1))
    }
}
