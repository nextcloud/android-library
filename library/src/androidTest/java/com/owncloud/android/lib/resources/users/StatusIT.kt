/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.users

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.status.OwnCloudVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StatusIT : AbstractIT() {
    companion object {
        const val SECOND_IN_MILLIS = 1000
        const val HOUR_IN_MINUTES = 3600
    }

    @Before
    fun before() {
        testOnlyOnServer(OwnCloudVersion.nextcloud_20)
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

        result =
            SetPredefinedCustomStatusMessageRemoteOperation(newCustomStatusMessage.id, clearAt)
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

        var result =
            SetUserDefinedCustomStatusMessageRemoteOperation(message, statusIcon, clearAt)
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
