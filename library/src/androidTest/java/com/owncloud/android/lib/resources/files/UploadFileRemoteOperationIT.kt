/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.files.model.RemoteFile
import com.owncloud.android.lib.resources.status.GetCapabilitiesRemoteOperation
import com.owncloud.android.lib.resources.status.NextcloudVersion
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.TimeUnit

class UploadFileRemoteOperationIT : AbstractIT() {
    private val capability = GetCapabilitiesRemoteOperation().execute(nextcloudClient).getResultData()

    @Test
    fun creationTime() {
        val imageFile = getFile("imageFile.png")
        val creationDate = getCreationTimestamp(imageFile)
        assertNotNull(creationDate)
        assertTrue(creationDate!! > (System.currentTimeMillis() / MILLI_TO_SECOND) - TIME_OFFSET)
    }

    @Test
    fun upload() {
        val remotePath = "/test.md"
        val createCollections = false
        upload(remotePath, createCollections)
    }

    @Test
    fun uploadAutoCreateFolderFlat() {
        if (!capability.version.isNewerOrEqual(NextcloudVersion.nextcloud_32)) {
            Log_OC.i(TAG, "Ignoring test due to unsupported Nextcloud version")
            return
        }

        val remotePath = "/testFolderFlat.md"
        val createCollections = true
        upload(remotePath, createCollections)
    }

    @Test
    fun uploadAutoCreateFolder() {
        if (!capability.version.isNewerOrEqual(NextcloudVersion.nextcloud_32)) {
            Log_OC.i(TAG, "Ignoring test due to unsupported Nextcloud version")
            return
        }

        val remotePath = "/someFolder/testInFolder.md"
        val createCollections = true
        upload(remotePath, createCollections)
    }

    @Test
    fun uploadAutoCreateFolderTree() {
        if (!capability.version.isNewerOrEqual(NextcloudVersion.nextcloud_32)) {
            Log_OC.i(TAG, "Ignoring test due to unsupported Nextcloud version")
            return
        }

        val remotePath = "/someFolder/someSubfolder/testInSubfolder.md"
        val createCollections = true
        upload(remotePath, createCollections)
    }

    private fun upload(
        remotePath: String,
        createCollections: Boolean
    ) {
        // create file
        val filePath = createFile("text")

        val creationTimestamp = getCreationTimestamp(File(filePath))
        val sut =
            UploadFileRemoteOperation(
                filePath,
                remotePath,
                "text/markdown",
                "",
                RANDOM_MTIME,
                creationTimestamp,
                true,
                createCollections
            )
        val uploadTimestamp = System.currentTimeMillis() / MILLI_TO_SECOND

        val uploadResult = sut.execute(client)
        assertTrue(uploadResult.isSuccess)

        // ReadFileRemoteOperation
        var result = ReadFileRemoteOperation(remotePath).execute(client)
        assertTrue(result.isSuccess)

        var remoteFile = result.data[0] as RemoteFile

        assertEquals(remotePath, remoteFile.remotePath)
        assertEquals(creationTimestamp, remoteFile.creationTimestamp)
        assertEquals(uploadResult.resultData, remoteFile.etag)
        assertTrue(
            uploadTimestamp - TIME_OFFSET < remoteFile.uploadTimestamp ||
                uploadTimestamp + TIME_OFFSET > remoteFile.uploadTimestamp
        )

        // ReadFolderRemoteOperation
        result = ReadFolderRemoteOperation(remotePath).execute(client)
        assertTrue(result.isSuccess)

        remoteFile = result.data[0] as RemoteFile

        assertEquals(remotePath, remoteFile.remotePath)
        assertEquals(creationTimestamp, remoteFile.creationTimestamp)
        assertTrue(
            uploadTimestamp - TIME_OFFSET < remoteFile.uploadTimestamp ||
                uploadTimestamp + TIME_OFFSET > remoteFile.uploadTimestamp
        )
    }

    private fun getCreationTimestamp(file: File): Long? =
        try {
            Files
                .readAttributes(file.toPath(), BasicFileAttributes::class.java)
                .creationTime()
                .to(TimeUnit.SECONDS)
        } catch (e: IOException) {
            Log_OC.e(
                UploadFileRemoteOperation::class.java.simpleName,
                "Failed to read creation timestamp for file: " + file.name
            )
            null
        }

    companion object {
        const val TIME_OFFSET = 10
        private val TAG = UploadFileRemoteOperationIT::class.simpleName
    }
}
