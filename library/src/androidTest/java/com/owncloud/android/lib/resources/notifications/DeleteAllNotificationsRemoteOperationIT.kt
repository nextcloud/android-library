/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias.kaminsky@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.notifications

import com.owncloud.android.AbstractIT
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteAllNotificationsRemoteOperationIT : AbstractIT() {
    @Test
    fun testDeleteAllNotification() {
        // create one notification
        assertTrue(
            nextcloudClientAdmin.execute(
                CreateNotificationRemoteOperation(
                    nextcloudClient.userId,
                    "test"
                )
            ).isSuccess
        )

        var result = nextcloudClient.execute(GetNotificationsRemoteOperation())
        assertTrue(result.isSuccess)
        assertTrue(result.resultData.isNotEmpty())

        assertTrue(nextcloudClient.execute(DeleteAllNotificationsRemoteOperation()).isSuccess)

        result = nextcloudClient.execute(GetNotificationsRemoteOperation())
        assertTrue(result.isSuccess)
        assertTrue(result.resultData.isEmpty())
    }
}
