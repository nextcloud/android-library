/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.files

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.files.ReadFileRemoteOperation
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation
import com.owncloud.android.lib.resources.files.model.FileLockType
import com.owncloud.android.lib.resources.files.model.RemoteFile
import com.owncloud.android.lib.resources.status.NextcloudVersion.Companion.nextcloud_24
import okhttp3.internal.http.HTTP_NOT_IMPLEMENTED
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

class ToggleFileLockRemoteOperationIT : AbstractIT() {
    @Test
    fun lockUnlockFile() {
        // only on >= NC24
        testOnlyOnServer(nextcloud_24)

        // create file
        val filePath: String = createFile("text")
        val remotePath = "/text.md"
        assertTrue(
            @Suppress("Detekt.MagicNumber")
            UploadFileRemoteOperation(filePath, remotePath, "text/markdown", 1464818400)
                .execute(client).isSuccess
        )
        val initialFile = ReadFileRemoteOperation(remotePath).execute(nextcloudClient).resultData as RemoteFile
        assertFalse("File shouldn't be locked", initialFile.isLocked)

        // lock file
        val lockResult = ToggleFileLockRemoteOperation(toLock = true, remotePath).execute(nextcloudClient)

        // check if file locking is enabled (files_lock app needs to be installed)
        assumeTrue(lockResult.httpCode != HTTP_NOT_IMPLEMENTED)

        assertTrue("File lock failed", lockResult.isSuccess)
        val lockFile = ReadFileRemoteOperation(remotePath).execute(nextcloudClient).resultData as RemoteFile
        assertTrue("File should be locked", lockFile.isLocked)
        assertEquals("Wrong lock type", FileLockType.MANUAL, lockFile.lockType)

        // unlock again
        val unlockResult = ToggleFileLockRemoteOperation(toLock = false, remotePath).execute(nextcloudClient)
        assertTrue("File unlock failed", unlockResult.isSuccess)
        val unlockFile = ReadFileRemoteOperation(remotePath).execute(nextcloudClient).resultData as RemoteFile
        assertFalse("File shouldn't be locked", unlockFile.isLocked)
    }
}
