/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.profile

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.status.NextcloudVersion.Companion.nextcloud_23
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetHoverCardRemoteOperationIT : AbstractIT() {
    @Before
    fun before() {
        requireServerVersion(nextcloud_23)
    }

    @Test
    fun testHoverCard() {
        val result =
            GetHoverCardRemoteOperation(nextcloudClient.userId)
                .execute(nextcloudClient)
        assertTrue(result.logMessage, result.isSuccess)
        val hoverCard = result.resultData
        assertEquals(nextcloudClient.userId, hoverCard?.userId)
    }
}
