/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias.kaminsky@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.notifications

import com.owncloud.android.AbstractIT
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteNotificationRemoteOperationIT : AbstractIT() {
    @Test
    fun deleteOneNotification() {
        // create one notification
        assertTrue(
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

        val firstNotificationId = result.resultData.first().notificationId

        assertTrue(DeleteNotificationRemoteOperation(firstNotificationId).execute(nextcloudClient).isSuccess)

        val getResult = GetNotificationRemoteOperation(firstNotificationId).execute(nextcloudClient)
        assertFalse(getResult.isSuccess)
    }
}
