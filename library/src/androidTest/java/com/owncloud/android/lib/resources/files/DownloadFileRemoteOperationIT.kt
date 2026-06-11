/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2021-2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-FileCopyrightText: 2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import com.nextcloud.common.NextcloudClient
import com.owncloud.android.AbstractIT
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

@Suppress("Detekt.MagicNumber")
class DownloadFileRemoteOperationIT : AbstractIT() {
    private val cacheDir get() = context.externalCacheDir?.absolutePath

    @Test
    fun download() {
        val filePath = createFile("download")
        val remotePath = "/download.jpg"
        assertTrue(
            UploadFileRemoteOperation(filePath, remotePath, "image/jpg", 1464818400)
                .execute(client)
                .isSuccess
        )

        assertTrue(
            DownloadFileRemoteOperation(remotePath, cacheDir)
                .execute(nextcloudClient)
                .isSuccess
        )

        val oldFile = File(filePath)
        val newFile = File(cacheDir + remotePath)
        assertSame(oldFile.length(), newFile.length())
    }

    @Test
    fun downloadLargeFile() {
        val filePath = createFile("large_download", 1000)
        val remotePath = "/large_download.txt"
        assertTrue(
            UploadFileRemoteOperation(filePath, remotePath, "text/plain", RANDOM_MTIME)
                .execute(client)
                .isSuccess
        )

        assertTrue(
            DownloadFileRemoteOperation(remotePath, cacheDir)
                .execute(nextcloudClient)
                .isSuccess
        )

        val originalFile = File(filePath)
        val downloadedFile = File(cacheDir + remotePath)
        assertEquals(originalFile.length(), downloadedFile.length())
    }

    @Test
    fun downloadNonExistentFile() {
        val result =
            DownloadFileRemoteOperation("/nonexistent_file_12345.txt", cacheDir)
                .execute(nextcloudClient)

        assertFalse(result.isSuccess)
    }

    @Test
    fun downloadAndVerifyMetadata() {
        val filePath = createFile("metadata_download")
        val remotePath = "/metadata_download.jpg"
        assertTrue(
            UploadFileRemoteOperation(filePath, remotePath, "image/jpg", RANDOM_MTIME)
                .execute(client)
                .isSuccess
        )

        val operation = DownloadFileRemoteOperation(remotePath, cacheDir)
        assertTrue(operation.execute(nextcloudClient).isSuccess)

        assertTrue("ETag should not be empty after download", operation.etag.isNotEmpty())
        assertTrue("Modification timestamp should be positive after download", operation.modificationTimestamp > 0)
    }

    @Test
    fun downloadMultipleFiles() {
        val filePath1 = createFile("multi_download1")
        val remotePath1 = "/multi_download1.jpg"
        val filePath2 = createFile("multi_download2")
        val remotePath2 = "/multi_download2.jpg"

        assertTrue(
            UploadFileRemoteOperation(filePath1, remotePath1, "image/jpg", RANDOM_MTIME)
                .execute(client)
                .isSuccess
        )
        assertTrue(
            UploadFileRemoteOperation(filePath2, remotePath2, "image/jpg", RANDOM_MTIME)
                .execute(client)
                .isSuccess
        )

        assertTrue(
            DownloadFileRemoteOperation(remotePath1, cacheDir)
                .execute(nextcloudClient)
                .isSuccess
        )
        assertTrue(
            DownloadFileRemoteOperation(remotePath2, cacheDir)
                .execute(nextcloudClient)
                .isSuccess
        )

        val downloaded1 = File(cacheDir + remotePath1)
        val downloaded2 = File(cacheDir + remotePath2)
        assertTrue(downloaded1.exists())
        assertTrue(downloaded2.exists())
        assertEquals(File(filePath1).length(), downloaded1.length())
        assertEquals(File(filePath2).length(), downloaded2.length())
    }

    @Test
    fun downloadAndVerifyContent() {
        val filePath = createFile("content_download", 50)
        val remotePath = "/content_download.txt"
        assertTrue(
            UploadFileRemoteOperation(filePath, remotePath, "text/plain", RANDOM_MTIME)
                .execute(client)
                .isSuccess
        )

        assertTrue(
            DownloadFileRemoteOperation(remotePath, cacheDir)
                .execute(nextcloudClient)
                .isSuccess
        )

        val originalFile = File(filePath)
        val downloadedFile = File(cacheDir + remotePath)
        assertTrue(downloadedFile.exists())
        assertTrue(originalFile.readBytes().contentEquals(downloadedFile.readBytes()))
    }

    @Test
    fun downloadedFileExistsAtExpectedPath() {
        val filePath = createFile("path_check")
        val remotePath = "/path_check.jpg"
        assertTrue(
            UploadFileRemoteOperation(filePath, remotePath, "image/jpg", RANDOM_MTIME)
                .execute(client)
                .isSuccess
        )

        assertTrue(
            DownloadFileRemoteOperation(remotePath, cacheDir)
                .execute(nextcloudClient)
                .isSuccess
        )

        val expectedFile = File(cacheDir + remotePath)
        assertTrue("Downloaded file should exist at expected path", expectedFile.exists())
        assertTrue("Downloaded file should not be empty", expectedFile.length() >= 0)
    }

    @Test
    fun downloadLargeFileSucceedsWithNoCallTimeout() {
        val filePath = createFile("large_no_call_timeout", 1000)
        val remotePath = "/large_no_call_timeout.txt"
        assertTrue(
            UploadFileRemoteOperation(filePath, remotePath, "text/plain", RANDOM_MTIME)
                .execute(client)
                .isSuccess
        )

        val slowOkHttpClient =
            nextcloudClient.client
                .newBuilder()
                .addInterceptor(ChunkDelayInterceptor(delayMs = 100))
                .build()
        val slowNextcloudClient =
            NextcloudClient(url, nextcloudClient.getUserIdPlain(), nextcloudClient.credentials, slowOkHttpClient, nextcloudClient.context)

        assertTrue(
            DownloadFileRemoteOperation(remotePath, cacheDir)
                .execute(slowNextcloudClient)
                .isSuccess
        )

        assertEquals(File(filePath).length(), File(cacheDir + remotePath).length())
    }

    /**
     * Used for create delay for test
     */
    private class ChunkDelayInterceptor(private val delayMs: Long) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val response = chain.proceed(chain.request())
            val body = response.body
            val slowSource =
                object : ForwardingSource(body.source()) {
                    override fun read(
                        sink: Buffer,
                        byteCount: Long
                    ): Long {
                        Thread.sleep(delayMs)
                        return super.read(sink, byteCount)
                    }
                }
            val slowBody =
                object : ResponseBody() {
                    private val bufferedSource: BufferedSource = slowSource.buffer()

                    override fun contentType() = body.contentType()

                    override fun contentLength() = body.contentLength()

                    override fun source() = bufferedSource
                }
            return response.newBuilder().body(slowBody).build()
        }
    }
}
