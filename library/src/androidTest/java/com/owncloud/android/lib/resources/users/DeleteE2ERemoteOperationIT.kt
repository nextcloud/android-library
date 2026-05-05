/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.users

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.e2ee.DeleteEncryptedFilesRemoteOperation
import junit.framework.TestCase.assertTrue
import org.junit.Test

class DeleteE2ERemoteOperationIT : AbstractIT() {
    @Test
    fun testDeleteEncryptedFiles() {
        val sut = DeleteEncryptedFilesRemoteOperation()
        val result = sut.execute(nextcloudClient)
        assertTrue(result.isSuccess)
    }

    @Test
    fun testDeletePrivateKey() {
        val sut = DeletePrivateKeyRemoteOperation()
        val result = sut.execute(nextcloudClient)
        assertTrue(result.isSuccess)
    }

    @Test
    fun testDeletePublicKey() {
        val sut = DeletePublicKeyRemoteOperation()
        val result = sut.execute(nextcloudClient)
        assertTrue(result.isSuccess)
    }
}
