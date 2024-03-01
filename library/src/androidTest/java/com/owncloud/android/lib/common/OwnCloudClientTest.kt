/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021-2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2022 Álvaro Brey Vilas <alvaro.brey@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common

import android.net.Uri
import com.nextcloud.common.NextcloudClient
import com.owncloud.android.AbstractIT
import junit.framework.Assert.assertEquals
import okhttp3.Credentials.basic
import org.junit.Test

class OwnCloudClientTest : AbstractIT() {
    @Test
    fun testUri() {
        val client =
            OwnCloudClientFactory.createOwnCloudClient(
                Uri.parse("https://10.0.2.2/nc"),
                context,
                false
            )
        client.userId = "test"

        assertEquals(
            Uri.parse("https://10.0.2.2/nc/remote.php/dav/uploads"),
            client.uploadUri
        )

        client.userId = "test@nextcloud.com"
        assertEquals(
            Uri.parse("https://10.0.2.2/nc/remote.php/dav/uploads"),
            client.uploadUri
        )
    }

    @Test
    fun testUserId() {
        val client =
            OwnCloudClientFactory.createOwnCloudClient(
                Uri.parse("https://10.0.2.2/nc"),
                context,
                false
            )

        val credentials = basic("user", "password")
        val nextcloudClient = NextcloudClient(url, "user", credentials, context)

        val testList =
            listOf(
                Pair("test@test.de", "test@test.de"),
                Pair("Test User", "Test%20User"),
                Pair("test", "test"),
                Pair("test+test@test.localhost", "test+test@test.localhost"),
                Pair("test - ab4c", "test%20-%20ab4c"),
                Pair("test.-&51_+-?@test.localhost", "test.-%2651_+-%3F@test.localhost"),
                Pair("test'ab4c", "test%27ab4c")
            )

        testList.forEach { pair ->
            client.userId = pair.first
            assertEquals("Wrong encoded userId with ownCloudClient", pair.second, client.userId)
            assertEquals(pair.first, client.userIdPlain)

            nextcloudClient.userId = pair.first
            assertEquals(
                "Wrong encoded userId with nextcloudClient",
                pair.second,
                nextcloudClient.getUserIdEncoded()
            )
            assertEquals(pair.first, nextcloudClient.getUserIdPlain())
        }
    }
}
