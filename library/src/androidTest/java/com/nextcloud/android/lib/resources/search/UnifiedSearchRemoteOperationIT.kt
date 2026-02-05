/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.search

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation
import com.owncloud.android.lib.resources.files.ReadFileRemoteOperation
import com.owncloud.android.lib.resources.files.model.RemoteFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UnifiedSearchRemoteOperationIT : AbstractIT() {
    @Test
    fun filesSearchEmptySearch() {
        val result = UnifiedSearchRemoteOperation("files", "").execute(nextcloudClient)
        assertFalse(result.isSuccess)
        assertTrue(result.exception is IllegalArgumentException)
    }

    @Test
    fun filesSearchBlankSearch() {
        val result = UnifiedSearchRemoteOperation("files", "   ").execute(nextcloudClient)
        assertFalse(result.isSuccess)
        assertTrue(result.exception is IllegalArgumentException)
    }

    @Test
    fun filesSearchEmptyResult() {
        val result = UnifiedSearchRemoteOperation("files", "test").execute(nextcloudClient)
        assertTrue(result.isSuccess)

        val data = result.resultData
        assertTrue(data.entries.isEmpty())
    }

    @Test
    fun filesSearch() {
        val remotePath = "/testFolder"
        assertTrue(CreateFolderRemoteOperation(remotePath, true).execute(client).isSuccess)
        val remoteFile =
            ReadFileRemoteOperation(remotePath)
                .execute(client)
                .data[0] as RemoteFile
        val fileId = remoteFile.localId

        val result = UnifiedSearchRemoteOperation("files", "test").execute(nextcloudClient)
        assertTrue(result.isSuccess)

        val data = result.resultData
        assertEquals("Files", data.name)
        assertTrue(data.entries.isNotEmpty())

        val firstResult = data.entries.find { it.title == "testFolder" }

        assertNotNull(firstResult)
        assertEquals(remotePath, firstResult?.remotePath())
        assertEquals(fileId.toString(), firstResult?.fileId())
    }

    @Test
    fun filesSearchWhitespace() {
        assertTrue(CreateFolderRemoteOperation("/test Folder/", true).execute(client).isSuccess)

        val result = UnifiedSearchRemoteOperation("files", "test").execute(nextcloudClient)
        assertTrue(result.isSuccess)

        val data = result.resultData
        assertTrue(data.name == "Files")
        assertTrue(data.entries.isNotEmpty())
        assertNotNull(data.entries.find { it.title == "test Folder" })
    }
}
