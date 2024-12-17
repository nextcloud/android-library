/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.notifications

import com.owncloud.android.AbstractIT
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class NotificationIT : AbstractIT() {
    @Test
    fun getNotification() {
        // get all
        val all = GetNotificationsRemoteOperation().execute(nextcloudClient)
        assertTrue(all.isSuccess)

        val count = all.resultData.size

        // get one
        assertTrue(count > 0)
        val firstNotification = all.resultData[0]
        val first = GetNotificationRemoteOperation(firstNotification.notificationId).execute(nextcloudClient)
        assertTrue(first.isSuccess)
        assertEquals(firstNotification.message, first.resultData.message)

        // delete one
        assertTrue(
            DeleteNotificationRemoteOperation(first.resultData.notificationId)
                .execute(nextcloudClient)
                .isSuccess
        )

        // get all, second run
        val all2 = GetNotificationsRemoteOperation().execute(nextcloudClient)
        assertTrue(all2.isSuccess)

        assertEquals(count - 1, all2.resultData.size)

        // delete all
        assertTrue(DeleteAllNotificationsRemoteOperation().execute(nextcloudClient).isSuccess)

        // get all, third run
        val all3 = GetNotificationsRemoteOperation().execute(nextcloudClient)
        assertTrue(all3.isSuccess)

        assertEquals(0, all3.resultData.size)
    }
}
