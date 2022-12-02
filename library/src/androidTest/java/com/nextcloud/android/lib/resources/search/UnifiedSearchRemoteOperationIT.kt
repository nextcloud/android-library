/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2020 Tobias Kaminsky
 * Copyright (C) 2020 Nextcloud GmbH
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
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
        val remoteFile = ReadFileRemoteOperation(remotePath)
            .execute(client).data[0] as RemoteFile
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
