/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2023 Tobias Kaminsky
 *   Copyright (C) 2023 Nextcloud GmbH
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

package com.owncloud.android.lib.resources.files.webdav

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.common.network.WebdavEntry
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode
import com.owncloud.android.lib.resources.files.ChunkedFileUploadRemoteOperation
import junit.framework.TestCase
import junit.framework.TestCase.assertNotNull
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod
import org.junit.Test
import java.io.File

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
    }
}
