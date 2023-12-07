/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2021 Tobias Kaminsky
 *   Copyright (C) 2021 Nextcloud GmbH
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

package com.owncloud.android.lib.resources.files

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.files.model.RemoteFile
import com.owncloud.android.lib.resources.status.GetCapabilitiesRemoteOperation
import com.owncloud.android.lib.resources.status.NextcloudVersion
import com.owncloud.android.lib.resources.status.OCCapability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.FileWriter

class ReadFileVersionsRemoteOperationIT : AbstractIT() {
    @Test
    fun listVersions() {
        val txtFile = getFile(ASSETS__TEXT_FILE_NAME)
        val filePath = "/test.md"

        var uploadResult =
            UploadFileRemoteOperation(
                txtFile.absolutePath,
                filePath,
                "txt/plain",
                System.currentTimeMillis() / MILLI_TO_SECOND
            )
                .execute(client)

        assertTrue("Error uploading file $filePath: $uploadResult", uploadResult.isSuccess)

        var remoteFile = ReadFileRemoteOperation(filePath).execute(client).data[0] as RemoteFile

        var sutResult = ReadFileVersionsRemoteOperation(remoteFile.localId).execute(client)

        assertTrue(sutResult.isSuccess)

        var versionCount = 0
        val ocCapability =
            GetCapabilitiesRemoteOperation()
                .execute(nextcloudClient).singleData as OCCapability
        if (ocCapability.version.isNewerOrEqual(NextcloudVersion.nextcloud_26)) {
            // with NC26+ we always have a starting version
            versionCount++
        }
        assertEquals(versionCount, sutResult.data.size)

        // modify file to have a version
        FileWriter(txtFile).apply {
            write("test\n")
            flush()
            close()
        }

        uploadResult =
            UploadFileRemoteOperation(
                txtFile.absolutePath,
                filePath,
                "txt/plain",
                System.currentTimeMillis() / MILLI_TO_SECOND
            )
                .execute(client)

        assertTrue("Error uploading file $filePath: $uploadResult", uploadResult.isSuccess)

        remoteFile = ReadFileRemoteOperation(filePath).execute(client).data[0] as RemoteFile

        sutResult = ReadFileVersionsRemoteOperation(remoteFile.localId).execute(client)

        assertTrue(sutResult.isSuccess)

        versionCount++
        assertEquals(versionCount, sutResult.data.size)
    }
}
