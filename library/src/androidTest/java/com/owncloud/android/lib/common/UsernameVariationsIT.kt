/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Álvaro Brey Vilas <alvaro.brey@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.files.ExistenceCheckRemoteOperation
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class UsernameVariationsIT(private val username: String) : AbstractIT() {
    companion object {
        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun data(): Collection<Array<Any>> =
            listOf(
                arrayOf("test"),
                arrayOf("test test"),
                arrayOf("test@test")
            )
    }

    @Test
    fun testExistenceCheckWithUsername() {
        val ocClient = OwnCloudClientFactory.createOwnCloudClient(url, context, true)
        ocClient.credentials = OwnCloudBasicCredentials(username, "test")
        ocClient.userId = username // for test same as userId

        val existenceCheck = ExistenceCheckRemoteOperation("/", false).execute(ocClient)
        assertTrue(existenceCheck.isSuccess)
    }
}
