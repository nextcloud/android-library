/*
 *
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2022 Tobias Kaminsky
 * Copyright (C) 2022 Nextcloud GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.nextcloud.android.lib.resources.dashboard

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.status.GetCapabilitiesRemoteOperation
import com.owncloud.android.lib.resources.status.NextcloudVersion
import com.owncloud.android.lib.resources.status.OCCapability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Test

class DashboardListWidgetsRemoteOperationIT : AbstractIT() {
    @Test
    fun list() {
        // only on NC25+
        val ocCapability = GetCapabilitiesRemoteOperation()
            .execute(nextcloudClient).singleData as OCCapability
        Assume.assumeTrue(ocCapability.version.isNewerOrEqual(NextcloudVersion.nextcloud_25))

        val result = DashboardListWidgetsRemoteOperation().execute(nextcloudClient)
        assertTrue(result.isSuccess)

        assertTrue(result.resultData.isNotEmpty())

        assertTrue(result.resultData["recommendations"]?.buttons?.getOrNull(0) == null)

        assertEquals(1, result.resultData["activity"]?.buttons?.size)
        assertTrue(result.resultData["activity"]?.buttons?.getOrNull(0)?.type == DashBoardButtonType.MORE)
        assertTrue(result.resultData["activity"]?.roundIcons == false)

        assertTrue(result.resultData["user_status"]?.roundIcons == true)
    }
}
