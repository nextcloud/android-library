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
package com.owncloud.android.lib.resources.comments

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.files.ReadFileRemoteOperation
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation
import com.owncloud.android.lib.resources.files.model.RemoteFile
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod
import org.junit.Test

class CommentFileRemoteOperationIT : AbstractIT() {
    @Test
    fun comment() {
        val filePath: String = createFile("commentFile")
        val remotePath = "/commentFile.txt"
        assertTrue(
            UploadFileRemoteOperation(filePath, remotePath, "image/jpg", RANDOM_MTIME)
                .execute(client).isSuccess
        )

        val readResult = ReadFileRemoteOperation(remotePath).execute(client)
        val remoteFile = readResult.data[0] as RemoteFile
        assertTrue(readResult.isSuccess)

        checkComments(0, remoteFile.localId)

        assertTrue(
            CommentFileRemoteOperation("test", remoteFile.localId)
                .execute(client)
                .isSuccess
        )

        checkComments(1, remoteFile.localId)
    }

    private fun checkComments(expectedComments: Int, fileId: Long) {
        val readComment =
            PropFindMethod(client.baseUri.toString() + "/remote.php/dav/comments/files/" + fileId)
        client.executeMethod(readComment)
        assertTrue(readComment.succeeded())

        // offset by 1, as "root" entry always exists if file exists
        assertEquals(expectedComments + 1, readComment.responseBodyAsMultiStatus.responses.size)
    }
}
