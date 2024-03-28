/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.lib.resources.users

import androidx.test.platform.app.InstrumentationRegistry
import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.users.GetUserInfoRemoteOperation
import okhttp3.Credentials
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetUserInfoRemoteOperationIT : AbstractIT() {
    @Test
    fun testGetUserNoQuota() {
        nextcloudClient.credentials = Credentials.basic("user1", "user1")
        val userInfoResult = GetUserInfoRemoteOperation().execute(nextcloudClient)
        assertTrue(userInfoResult.isSuccess)

        val userInfo = userInfoResult.resultData
        assertEquals("User One", userInfo?.displayName)
        assertEquals("user1", userInfo?.id)
        assertEquals(GetUserInfoRemoteOperation.SPACE_UNLIMITED, userInfo?.quota?.quota)
    }

    @Test
    fun testGetUser1GbQuota() {
        nextcloudClient.credentials = Credentials.basic("user2", "user2")
        val userInfoResult = GetUserInfoRemoteOperation().execute(nextcloudClient)
        assertTrue(userInfoResult.isSuccess)
        val userInfo = userInfoResult.resultData

        assertEquals("User Two", userInfo?.displayName)
        assertEquals("user2", userInfo?.id)
        assertEquals(QUOTA_1GB, userInfo?.quota?.quota)
    }

    @After
    fun resetCredentials() {
        val arguments = InstrumentationRegistry.getArguments()

        val loginName = arguments.getString("TEST_SERVER_USERNAME")
        val password = arguments.getString("TEST_SERVER_PASSWORD")

        nextcloudClient.credentials = Credentials.basic(loginName.orEmpty(), password.orEmpty())
    }

    companion object {
        const val QUOTA_1GB = 1073741824L
    }
}
