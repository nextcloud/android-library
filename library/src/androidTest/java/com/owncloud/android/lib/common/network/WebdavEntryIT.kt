/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.network

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.files.ReadFileRemoteOperation
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation
import org.junit.Assert.assertTrue
import org.junit.Test

class WebdavEntryIT : AbstractIT() {
    @Test
    fun testRootMetadataAndListing() {
        val metadata = ReadFileRemoteOperation(ROOT_PATH).execute(client)
        assertTrue("Root metadata request failed", metadata.isSuccess)

        val listing = ReadFolderRemoteOperation(ROOT_PATH).execute(client)
        assertTrue("Root listing request failed", listing.isSuccess)
    }

    companion object {
        private const val ROOT_PATH = "/"
    }
}
