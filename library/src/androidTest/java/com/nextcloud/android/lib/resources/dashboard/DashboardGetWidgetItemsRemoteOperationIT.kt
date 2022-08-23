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
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardGetWidgetItemsRemoteOperationIT : AbstractIT() {
    @Test
    fun getItems() {
        val widgetId = "recommendations"
        val result =
            DashboardGetWidgetItemsRemoteOperation(widgetId, LIMIT_SIZE).execute(nextcloudClient)

        assertTrue(result.isSuccess)
        assertTrue(result.resultData[widgetId]?.isNotEmpty() ?: false)

        val firstResult = result.resultData[widgetId]?.get(0)
        assertTrue(firstResult?.title?.isNotEmpty() == true)
        assertTrue(firstResult?.subtitle != null)
        assertTrue(firstResult?.link?.isNotEmpty() == true)
        assertTrue(firstResult?.iconUrl?.isNotEmpty() == true)
    }

    @Test
    fun getEmptyItems() {
        val widgetId = "nonExistingWidget"
        val result =
            DashboardGetWidgetItemsRemoteOperation(widgetId, LIMIT_SIZE).execute(nextcloudClient)

        assertTrue(result.isSuccess)
        assertTrue(result.resultData.isEmpty())
    }

    companion object {
        const val LIMIT_SIZE = 14
    }
}
