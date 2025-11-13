/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.dashboard

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.status.NextcloudVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardListWidgetsRemoteOperationIT : AbstractIT() {
    @Test
    fun list() {
        // only on NC25+
        testOnlyOnServer(NextcloudVersion.nextcloud_25)

        val result = DashboardListWidgetsRemoteOperation().execute(nextcloudClient)
        assertTrue(result.isSuccess)
        val data = result.resultData
        assertTrue(data.isNotEmpty())
        assertTrue(data["recommendations"]?.buttons?.getOrNull(0) == null)

        val activityData = data["activity"]
        assertEquals(1, activityData?.buttons?.size)
        assertTrue(
            activityData
                ?.buttons
                ?.getOrNull(0)
                ?.type == DashBoardButtonType.MORE
        )
        assertTrue(activityData?.roundIcons != null)

        val userStatusData = data["user_status"]
        assertTrue(userStatusData?.roundIcons != null)
    }
}
