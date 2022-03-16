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
import com.owncloud.android.lib.resources.status.GetCapabilitiesRemoteOperation
import com.owncloud.android.lib.resources.status.OCCapability
import com.owncloud.android.lib.resources.status.OwnCloudVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test

class StatusIT : AbstractIT() {
    companion object {
        const val SECOND_IN_MILLIS = 1000
        const val HOUR_IN_MINUTES = 3600
    }

    @Before
    fun before() {
        val result = GetCapabilitiesRemoteOperation().execute(nextcloudClient)
        assertTrue(result.isSuccess)
        val ocCapability = result.singleData as OCCapability

        assumeTrue(ocCapability.version.isNewerOrEqual(OwnCloudVersion.nextcloud_20))
    }

    @Test
    fun getStatus() {
        val result = GetStatusRemoteOperation().run(nextcloudClient)
        assertTrue("GetStatusRemoteOperation failed: " + result.logMessage, result.isSuccess)

        val status = result.resultData
        assertTrue(status.message.isNullOrBlank())
    }

    @Test
    fun setStatus() {
        clearStatusMessage()
        val result0 = SetStatusRemoteOperation(StatusType.ONLINE).execute(nextcloudClient)
        assertTrue("SetStatusRemoteOperation failed: " + result0.logMessage, result0.isSuccess)

        for (statusType in StatusType.values()) {
            val result1 = GetStatusRemoteOperation().run(nextcloudClient)
            assertTrue("GetStatusRemoteOperation failed: " + result1.logMessage, result1.isSuccess)

            val result2 = SetStatusRemoteOperation(statusType).execute(nextcloudClient)
            assertTrue("SetStatusRemoteOperation failed: " + result2.logMessage, result2.isSuccess)

            val result3 = GetStatusRemoteOperation().run(nextcloudClient)
            assertTrue("GetStatusRemoteOperation failed: " + result3.logMessage, result3.isSuccess)

            val status = result3.resultData
            assertEquals(statusType, status.status)
        }

        val result2 = SetStatusRemoteOperation(StatusType.AWAY).run(nextcloudClient)
        assertTrue("SetStatusRemoteOperation failed: " + result2.logMessage, result2.isSuccess)

        clearStatusMessage()
    }

    @Test
    fun getPredefinedStatuses() {
        val result = GetPredefinedStatusesRemoteOperation().run(nextcloudClient)
        assertTrue(
            "GetPredefinedStatusesRemoteOperation failed: " + result.logMessage,
            result.isSuccess
        )

        val statusesList = result.resultData
        assertTrue(statusesList.isNotEmpty())
    }

    @Test
    fun clearStatusMessage() {
        val result = ClearStatusMessageRemoteOperation().execute(nextcloudClient)
        assertTrue(
            "ClearStatusMessageRemoteOperation failed: " + result.logMessage,
            result.isSuccess
        )

        // verify
        getStatus()
    }

    @Test
    fun setPredefinedCustomStatusMessage() {
        clearStatusMessage()

        var result = GetPredefinedStatusesRemoteOperation().run(nextcloudClient)
        assertTrue(
            "GetPredefinedStatusesRemoteOperation failed: " + result.logMessage,
            result.isSuccess
        )

        val statusesList: ArrayList<PredefinedStatus> = result.resultData
        val newCustomStatusMessage = statusesList[2]
        val clearAt = System.currentTimeMillis() / SECOND_IN_MILLIS + HOUR_IN_MINUTES

        result = SetPredefinedCustomStatusMessageRemoteOperation(newCustomStatusMessage.id, clearAt)
            .execute(nextcloudClient)
        assertTrue(
            "SetPredefinedCustomStatusMessageRemoteOperation failed: " + result.logMessage,
            result.isSuccess
        )

        // verify
        val newResult = GetStatusRemoteOperation().run(nextcloudClient)
        assertTrue("GetStatusRemoteOperation failed: " + newResult.logMessage, newResult.isSuccess)

        val status = newResult.resultData
        assertEquals(newCustomStatusMessage.message, status.message)
    }

    @Test
    fun setUserDefinedCustomStatusMessage() {
        clearStatusMessage()

        val message = "This is a test"
        val statusIcon = "☁"
        val clearAt = System.currentTimeMillis() / SECOND_IN_MILLIS + HOUR_IN_MINUTES

        var result = SetUserDefinedCustomStatusMessageRemoteOperation(message, statusIcon, clearAt)
            .execute(nextcloudClient)

        assertTrue(
            "SetUserDefinedCustomStatusMessageRemoteOperation failed: " + result.logMessage,
            result.isSuccess
        )

        // verify
        result = GetStatusRemoteOperation().run(nextcloudClient)
        assertTrue("GetStatusRemoteOperation failed: " + result.logMessage, result.isSuccess)

        val status = result.resultData
        assertEquals(message, status.message)
        assertEquals(statusIcon, status.icon)
        assertEquals(clearAt, status.clearAt)
    }
}
