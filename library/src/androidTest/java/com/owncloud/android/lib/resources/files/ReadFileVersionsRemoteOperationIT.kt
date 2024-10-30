/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.status.GetCapabilitiesRemoteOperation
import com.owncloud.android.lib.resources.status.NextcloudVersion
import com.owncloud.android.lib.resources.status.OCCapability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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

        var remoteFile = ReadFileRemoteOperation(filePath).execute(nextcloudClient).resultData
        assertNotNull(remoteFile)

        var sutResult = ReadFileVersionsRemoteOperation(remoteFile!!.localId).execute(client)

        assertTrue(sutResult.isSuccess)

        var versionCount = 0
        val ocCapability =
            GetCapabilitiesRemoteOperation()
                .execute(nextcloudClient).resultData as OCCapability
        if (ocCapability.version.isNewerOrEqual(NextcloudVersion.nextcloud_26)) {
            // with NC26+ we always have a starting version
            versionCount++
        }
        assertEquals(versionCount, sutResult.resultData?.size)

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

        remoteFile = ReadFileRemoteOperation(filePath).execute(nextcloudClient).resultData
        assertNotNull(remoteFile)

        sutResult = ReadFileVersionsRemoteOperation(remoteFile!!.localId).execute(client)

        assertTrue(sutResult.isSuccess)

        versionCount++
        assertEquals(versionCount, sutResult.resultData?.size)
    }
}
