/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files.webdav

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.common.network.WebdavEntry
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode
import com.owncloud.android.lib.resources.files.ChunkedFileUploadRemoteOperation
import com.owncloud.android.lib.resources.files.FileUtils
import junit.framework.TestCase
import junit.framework.TestCase.assertNotNull
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.methods.PutMethod
import org.apache.commons.httpclient.methods.StringRequestEntity
import org.apache.jackrabbit.webdav.DavConstants
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.net.URLEncoder

class ChunkedFileUploadRemoteOperationIT : AbstractIT() {
    @Test
    fun uploadWifi() {
        val sut = genLargeUpload(true)
        val uploadResult = sut.execute(client)
        assert(uploadResult.isSuccess)
    }

    @Test
    fun uploadMobile() {
        val sut = genLargeUpload(false)
        val uploadResult = sut.execute(client)
        assert(uploadResult.isSuccess)
    }

    @Test
    fun cancel() {
        val sut = genLargeUpload(false)

        var uploadResult: RemoteOperationResult<String>? = null
        Thread {
            uploadResult = sut.execute(client)
        }.start()

        shortSleep()
        sut.cancel(ResultCode.CANCELLED)

        for (i in 1..MAX_TRIES) {
            shortSleep()

            if (uploadResult != null) {
                break
            }
        }

        assertNotNull(uploadResult)
        TestCase.assertFalse(uploadResult?.isSuccess == true)
        TestCase.assertSame(ResultCode.CANCELLED, uploadResult?.code)
    }

    @Test
    fun resume() {
        val filePath = createFile("chunkedFile.txt", BIG_FILE_ITERATION * 2)
        val timestamp = System.currentTimeMillis() / MILLI_TO_SECOND
        val remotePath = "/bigFile.md"

        // set up first upload
        var sut =
            ChunkedFileUploadRemoteOperation(
                filePath,
                remotePath,
                "text/markdown",
                "",
                RANDOM_MTIME,
                timestamp,
                false,
                true
            )

        // start first upload
        var uploadResult: RemoteOperationResult<String>? = null
        Thread {
            uploadResult = sut.execute(client)
        }.start()

        // delay and cancel upload
        shortSleep()
        shortSleep()
        sut.cancel(ResultCode.CANCELLED)

        for (i in 1..MAX_TRIES) {
            shortSleep()

            if (uploadResult != null) {
                break
            }
        }

        // start second upload of same file
        sut =
            ChunkedFileUploadRemoteOperation(
                filePath,
                remotePath,
                "text/markdown",
                "",
                RANDOM_MTIME,
                timestamp,
                false,
                true
            )

        // reset result; start second upload
        uploadResult = null
        uploadResult = sut.execute(client)

        // second upload should succeed
        assert(uploadResult?.isSuccess == true)

        assert(File(filePath).length() == getRemoteSize(remotePath))
    }

    @Test
    fun uploadExceedingQuotaIsRejectedBeforeAnyChunkIsStored() {
        val filePath = createFile("chunkedFile.txt", BIG_FILE_ITERATION)
        val uploadFolderUri = "${client.uploadUri}/${client.userId}/${FileUtils.md5Sum(File(filePath))}"
        val sut =
            ChunkedFileUploadRemoteOperation(
                filePath,
                "/bigFile.md",
                "text/markdown",
                "",
                RANDOM_MTIME,
                System.currentTimeMillis() / MILLI_TO_SECOND,
                false,
                true
            )

        setQuota(QUOTA_SMALLER_THAN_FILE)
        try {
            val uploadResult = sut.execute(client)

            assertEquals(ResultCode.QUOTA_EXCEEDED, uploadResult.code)
            assertEquals(0, storedChunkCount(uploadFolderUri))
        } finally {
            setQuota(QUOTA_UNLIMITED)
            DeleteMethod(uploadFolderUri).also {
                client.executeMethod(it)
                it.releaseConnection()
            }
        }
    }

    private fun setQuota(quota: String) {
        val method = PutMethod("${client2.baseUri}/ocs/v2.php/cloud/users/${client.userId}")
        method.addRequestHeader(RemoteOperation.OCS_API_HEADER, RemoteOperation.OCS_API_HEADER_VALUE)
        method.requestEntity =
            StringRequestEntity(
                "key=quota&value=${URLEncoder.encode(quota, Charsets.UTF_8.name())}",
                "application/x-www-form-urlencoded",
                Charsets.UTF_8.name()
            )

        try {
            assertEquals(HttpStatus.SC_OK, client2.executeMethod(method))
        } finally {
            method.releaseConnection()
        }
    }

    private fun storedChunkCount(uploadFolderUri: String): Int {
        val propFind = PropFindMethod(uploadFolderUri, WebdavUtils.getChunksPropSet(), DavConstants.DEPTH_1)

        try {
            client.executeMethod(propFind)
            assertTrue(propFind.succeeded())
            return propFind.responseBodyAsMultiStatus.responses.size - UPLOAD_FOLDER_ENTRY
        } finally {
            propFind.releaseConnection()
        }
    }

    private fun genLargeUpload(onWifiConnection: Boolean): ChunkedFileUploadRemoteOperation {
        // create file
        val filePath = createFile("chunkedFile.txt", BIG_FILE_ITERATION)
        val remotePath = "/bigFile.md"

        return ChunkedFileUploadRemoteOperation(
            filePath,
            remotePath,
            "text/markdown",
            "",
            RANDOM_MTIME,
            System.currentTimeMillis() / MILLI_TO_SECOND,
            onWifiConnection,
            true
        )
    }

    private fun getRemoteSize(remotePath: String): Long {
        val davPath = client.filesDavUri.toString() + "/" + WebdavUtils.encodePath(remotePath)
        val propFindMethod = PropFindMethod(davPath, WebdavUtils.getFilePropSet(), 0)
        client.executeMethod(propFindMethod)
        assert(propFindMethod.succeeded())

        return WebdavEntry(propFindMethod.responseBodyAsMultiStatus.responses[0], remotePath).contentLength
    }

    companion object {
        const val BIG_FILE_ITERATION = 1000000
        const val MAX_TRIES = 30
        const val QUOTA_SMALLER_THAN_FILE = "1 MB"
        const val QUOTA_UNLIMITED = "none"
        const val UPLOAD_FOLDER_ENTRY = 1
    }
}
