/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2020 Tobias Kaminsky
 * Copyright (C) 2020 Nextcloud GmbH
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 *  
 */
package com.owncloud.android.lib.resources.users

import com.owncloud.android.AbstractIT
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StatusIT : AbstractIT() {
    @Test
    fun getStatus() {
        val result = GetStatusRemoteOperation().run(nextcloudClient)
        assertTrue(result.isSuccess)

        val status = result.singleData as Status
        assertTrue(status.message.isNullOrBlank())
    }

    @Test
    fun setStatus() {
        clearStatusMessage()
        assertTrue(SetStatusRemoteOperation(StatusType.online).execute(nextcloudClient).isSuccess)

        for (statusType in StatusType.values()) {
            var result = GetStatusRemoteOperation().run(nextcloudClient)
            assertTrue(result.isSuccess)

            assertTrue(SetStatusRemoteOperation(statusType).execute(nextcloudClient).isSuccess)

            result = GetStatusRemoteOperation().run(nextcloudClient)
            assertTrue(result.isSuccess)

            val status = result.singleData as Status
            assertEquals(statusType, status.status)
        }

        assertTrue(SetStatusRemoteOperation(StatusType.away).execute(nextcloudClient).isSuccess)
        clearStatusMessage()
    }

    @Test
    fun getPredefinedStatuses() {
        val result = GetPredefinedStatusesRemoteOperation().run(nextcloudClient)
        assertTrue(result.isSuccess)

        val statusesList: ArrayList<PredefinedStatus> = result.singleData as ArrayList<PredefinedStatus>
        assertTrue(statusesList.isNotEmpty())
    }

    @Test
    fun clearStatusMessage() {
        assertTrue(ClearStatusMessageRemoteOperation().execute(nextcloudClient).isSuccess)

        // verify
        getStatus()
    }

    @Test
    fun setPredefinedCustomStatusMessage() {
        clearStatusMessage()

        var result = GetPredefinedStatusesRemoteOperation().run(nextcloudClient)
        assertTrue(result.isSuccess)

        val statusesList: ArrayList<PredefinedStatus> = result.singleData as ArrayList<PredefinedStatus>
        val newCustomStatusMessage = statusesList[2]
        val clearAt = System.currentTimeMillis() / 1000 + 3600 // in one hour

        assertTrue(SetPredefinedCustomStatusMessageRemoteOperation(newCustomStatusMessage.id, clearAt)
                .execute(nextcloudClient)
                .isSuccess)

        // verify
        result = GetStatusRemoteOperation().run(nextcloudClient)
        assertTrue(result.isSuccess)

        val status = result.singleData as Status
        assertEquals(newCustomStatusMessage.message, status.message)
    }

    @Test
    fun setUserDefinedCustomStatusMessage() {
        clearStatusMessage()

        val message = "This is a test"
        val statusIcon = "☁"
        val clearAt = System.currentTimeMillis() / 1000 + 3600 // in one hour

        assertTrue(SetUserDefinedCustomStatusMessageRemoteOperation(message, statusIcon, clearAt)
                .execute(nextcloudClient)
                .isSuccess)

        // verify
        val result = GetStatusRemoteOperation().run(nextcloudClient)
        assertTrue(result.isSuccess)

        val status = result.singleData as Status
        assertEquals(message, status.message)
        assertEquals(statusIcon, status.icon)
        assertEquals(clearAt, status.clearAt)
    }
}
