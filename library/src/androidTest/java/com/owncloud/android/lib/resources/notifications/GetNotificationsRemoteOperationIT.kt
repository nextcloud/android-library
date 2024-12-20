/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias.kaminsky@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.notifications

import com.owncloud.android.AbstractIT
import junit.framework.Assert.assertTrue
import org.junit.Assert
import org.junit.Test

class GetNotificationsRemoteOperationIT : AbstractIT() {
    @Test
    fun testNotifications() {
        // create one notification
        Assert.assertTrue(
            nextcloudClientAdmin.execute(
                CreateNotificationRemoteOperation(
                    nextcloudClient.userId,
                    "test"
                )
            ).isSuccess
        )

        val result = nextcloudClient.execute(GetNotificationsRemoteOperation())
        assertTrue(result.isSuccess)
        assertTrue(result.resultData.isNotEmpty())
    }
}
