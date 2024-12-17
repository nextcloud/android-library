/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias.kaminsky@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.notifications

import com.owncloud.android.AbstractIT
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetNotificationRemoteOperationIT : AbstractIT() {
    @Test
    fun testSingleNotification() {
        // get all notifications
        val resultAll = nextcloudClient.execute(GetNotificationsRemoteOperation())
        assertTrue(resultAll.isSuccess)

        val allNotifications = resultAll.resultData
        val firstNotificationId = allNotifications.first().notificationId

        // check one specific
        val result = nextcloudClient.execute(GetNotificationRemoteOperation(firstNotificationId))
        assertTrue(result.isSuccess)

        val notification = result.resultData
        assertNotNull(notification)
        assertEquals(firstNotificationId, notification.notificationId)
    }

    @Test
    fun testNonExisting() {
        assertFalse(GetNotificationRemoteOperation(-1).execute(nextcloudClient).isSuccess)
    }
}
