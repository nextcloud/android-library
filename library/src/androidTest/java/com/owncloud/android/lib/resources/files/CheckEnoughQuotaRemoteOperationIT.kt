/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.common.OwnCloudBasicCredentials
import com.owncloud.android.lib.common.OwnCloudClientFactory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckEnoughQuotaRemoteOperationIT : AbstractIT() {
    @Test
    fun enoughQuota() {
        val sut = CheckEnoughQuotaRemoteOperation("/", LARGE_FILE).execute(client)
        assertTrue(sut.isSuccess)
    }

    @Test
    fun noQuota() {
        // user3 has only 1M quota
        val client3 = OwnCloudClientFactory.createOwnCloudClient(url, context, true)
        client3.credentials = OwnCloudBasicCredentials("user3", "user3")
        val sut = CheckEnoughQuotaRemoteOperation("/", LARGE_FILE).execute(client3)
        assertFalse(sut.isSuccess)
    }

    companion object {
        const val LARGE_FILE = 5 * 1024 * 1024L
    }
}
