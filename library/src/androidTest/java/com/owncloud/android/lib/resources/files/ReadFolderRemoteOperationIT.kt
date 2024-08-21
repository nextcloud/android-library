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
import com.owncloud.android.lib.resources.status.NextcloudVersion
import com.owncloud.android.lib.resources.tags.CreateTagRemoteOperation
import com.owncloud.android.lib.resources.tags.GetTagsRemoteOperation
import com.owncloud.android.lib.resources.tags.PutTagRemoteOperation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadFolderRemoteOperationIT : AbstractIT() {
    companion object {
        const val TAG_LENGTH = 10
    }

    @Test
    fun readRemoteFolderWithContent() {
        val remotePath = "/test/"

        assertTrue(CreateFolderRemoteOperation(remotePath, true).execute(nextcloudClient).isSuccess)

        // create file
        val filePath = createFile("text")
        assertTrue(
            UploadFileRemoteOperation(filePath, remotePath + "1.txt", "text/markdown", RANDOM_MTIME)
                .execute(client).isSuccess
        )

        var result = ReadFolderRemoteOperation(remotePath).execute(nextcloudClient)

        assertTrue(result.isSuccess)
        assertEquals(2, result.resultData?.size)

        // tag testing only on NC27+
        testOnlyOnServer(NextcloudVersion.nextcloud_27)

        // Folder
        var remoteFolder = result.resultData?.get(0)
        assertEquals(remotePath, remoteFolder?.remotePath)
        assertEquals(0, remoteFolder?.tags?.size)

        // File
        var remoteFile = result.resultData?.get(1)
        assertNotNull(remoteFile)
        assertEquals(remotePath + "1.txt", remoteFile?.remotePath)
        assertEquals(0, remoteFile?.tags?.size)

        // create tag
        val tag1name = RandomStringGenerator.make(TAG_LENGTH)
        val tag2name = RandomStringGenerator.make(TAG_LENGTH)
        assertTrue(CreateTagRemoteOperation(tag1name).execute(nextcloudClient).isSuccess)
        assertTrue(CreateTagRemoteOperation(tag2name).execute(nextcloudClient).isSuccess)

        // list tags
        val tags = GetTagsRemoteOperation().execute(nextcloudClient).resultData
        assertNotNull(tags)

        // extract and check tags
        val tag1 =
            tags?.firstOrNull { tag ->
                tag.name == tag1name
            }
        assertNotNull(tag1)

        val tag2 =
            tags?.firstOrNull { tag ->
                tag.name == tag2name
            }
        assertNotNull(tag2)

        // add tag
        assertTrue(
            PutTagRemoteOperation(
                tag1!!.id,
                remoteFile!!.localId
            ).execute(nextcloudClient).isSuccess
        )

        assertTrue(
            PutTagRemoteOperation(
                tag2!!.id,
                remoteFile.localId
            ).execute(nextcloudClient).isSuccess
        )

        // check again
        result = ReadFolderRemoteOperation(remotePath).execute(nextcloudClient)

        assertTrue(result.isSuccess)
        assertEquals(2, result.resultData?.size)

        // Folder
        remoteFolder = result.resultData?.get(0)
        assertEquals(remotePath, remoteFolder?.remotePath)
        assertEquals(0, remoteFolder?.tags?.size)

        // File
        remoteFile = result.resultData?.get(1)
        assertEquals(remotePath + "1.txt", remoteFile?.remotePath)
        assertEquals(2, remoteFile?.tags?.size)

        // check that tags are set correctly
        assertTrue(remoteFile?.tags?.contentEquals(arrayOf(tag1name, tag2name)) == true)
    }
}
