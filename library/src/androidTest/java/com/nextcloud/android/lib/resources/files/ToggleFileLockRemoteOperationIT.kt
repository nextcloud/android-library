/*
 *  Nextcloud Android Library is available under MIT license
 *
 *  @author Álvaro Brey Vilas
 *  Copyright (C) 2022 Álvaro Brey Vilas
 *  Copyright (C) 2022 Nextcloud GmbH
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *  BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *  ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package com.nextcloud.android.lib.resources.files

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.files.ReadFileRemoteOperation
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation
import com.owncloud.android.lib.resources.files.model.FileLockType
import com.owncloud.android.lib.resources.files.model.RemoteFile
import com.owncloud.android.lib.resources.status.GetCapabilitiesRemoteOperation
import com.owncloud.android.lib.resources.status.NextcloudVersion.Companion.nextcloud_24
import com.owncloud.android.lib.resources.status.OCCapability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Test

class ToggleFileLockRemoteOperationIT : AbstractIT() {

    @Test
    fun lockUnlockFile() {
        // only on >= NC24
        val ocCapability = GetCapabilitiesRemoteOperation()
            .execute(nextcloudClient).singleData as OCCapability
        Assume.assumeTrue(
            ocCapability.version.isNewerOrEqual(nextcloud_24)
        )

        // create file
        val filePath: String = createFile("text")
        val remotePath = "/text.md"
        assertTrue(
            UploadFileRemoteOperation(filePath, remotePath, "text/markdown", "1464818400")
                .execute(client).isSuccess
        )
        val initialFile =
            ReadFileRemoteOperation(remotePath).execute(client).singleData as RemoteFile
        assertFalse("File shouldn't be locked", initialFile.isLocked)

        // lock file
        val lockResult = ToggleFileLockRemoteOperation(toLock = true, remotePath).execute(nextcloudClient)
        assertTrue("File lock failed", lockResult.isSuccess)
        val lockFile = ReadFileRemoteOperation(remotePath).execute(client).singleData as RemoteFile
        assertTrue("File should be locked", lockFile.isLocked)
        assertEquals("Wrong lock type", FileLockType.MANUAL, lockFile.lockType)

        // unlock again
        val unlockResult = ToggleFileLockRemoteOperation(toLock = false, remotePath).execute(nextcloudClient)
        assertTrue("File unlock failed", unlockResult.isSuccess)
        val unlockFile = ReadFileRemoteOperation(remotePath).execute(client).singleData as RemoteFile
        assertFalse("File shouldn't be locked", unlockFile.isLocked)
    }
}
