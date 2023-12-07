/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2020 Tobias Kaminsky
 *   Copyright (C) 2020 Nextcloud GmbH
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
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
        val remoteFile =
            ReadFileRemoteOperation(remotePath)
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
