/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.resources.files.model.RemoteFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckEtagRemoteOperationIT : AbstractIT() {
    @Test
    fun checkEtag() {
        val filePath = createFile("eTagFile")
        val remotePath = "/eTagFile.txt"
        assertTrue(
            @Suppress("Detekt.MagicNumber")
            UploadFileRemoteOperation(filePath, remotePath, "image/jpg", 1464818400)
                .execute(client)
                .isSuccess
        )

        val readResult = ReadFileRemoteOperation(remotePath).execute(client)
        val remoteFile = readResult.data[0] as RemoteFile
        val eTag = remoteFile.etag

        var eTagResult = CheckEtagRemoteOperation(remotePath, eTag).execute(client)
        assertEquals(RemoteOperationResult.ResultCode.ETAG_UNCHANGED, eTagResult.code)

        eTagResult = CheckEtagRemoteOperation(remotePath, "wrongEtag").execute(client)
        assertEquals(RemoteOperationResult.ResultCode.ETAG_CHANGED, eTagResult.code)
    }
}
