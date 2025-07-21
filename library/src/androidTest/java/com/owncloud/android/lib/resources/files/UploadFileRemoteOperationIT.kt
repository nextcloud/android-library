/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import android.os.Build
import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.common.OwnCloudBasicCredentials
import com.owncloud.android.lib.common.OwnCloudClientFactory
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.files.model.RemoteFile
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.TimeUnit

class UploadFileRemoteOperationIT : AbstractIT() {
    @Test
    fun creationTime() {
        val imageFile = getFile("imageFile.png")
        val creationDate = getCreationTimestamp(imageFile)
        assertNotNull(creationDate)
        assertTrue(creationDate!! > (System.currentTimeMillis() / MILLI_TO_SECOND) - TIME_OFFSET)
    }

    @Test
    fun upload() {
        // create file
        val filePath = createFile("text")
        val remotePath = "/test.md"

        val creationTimestamp = getCreationTimestamp(File(filePath))
        val sut =
            UploadFileRemoteOperation(
                filePath,
                remotePath,
                "text/markdown",
                "",
                RANDOM_MTIME,
                creationTimestamp,
                true
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

    @Throws(Throwable::class)
    @Test
    fun uploadFileWithQuotaExceeded() {
        // user3 has quota of 1Mb
        val client3 = OwnCloudClientFactory.createOwnCloudClient(url, context, true)
        client3.credentials = OwnCloudBasicCredentials("user3", "user3")
        client3.userId = "user3"

        // create file
        val filePath = createFile("quota", LARGE_FILE)
        val remotePath = "/quota.md"

        val creationTimestamp = getCreationTimestamp(File(filePath))
        val sut =
            UploadFileRemoteOperation(
                filePath,
                remotePath,
                "text/markdown",
                "",
                RANDOM_MTIME,
                creationTimestamp,
                true
            )

        val uploadResult = sut.execute(client3)
        assertFalse(uploadResult.isSuccess)
        assertEquals(ResultCode.QUOTA_EXCEEDED, uploadResult.code)
    }

    private fun getCreationTimestamp(file: File): Long? {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return null
        } else {
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
        }
    }

    companion object {
        const val TIME_OFFSET = 10
        const val LARGE_FILE = 10 * 1024
    }
}
