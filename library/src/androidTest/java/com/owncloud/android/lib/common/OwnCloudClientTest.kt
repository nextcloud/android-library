/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2021 Tobias Kaminsky
 *   Copyright (C) 2021 Nextcloud GmbH
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
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
        val client = OwnCloudClientFactory.createOwnCloudClient(
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
        val client = OwnCloudClientFactory.createOwnCloudClient(
            Uri.parse("https://10.0.2.2/nc"),
            context,
            false
        )

        val credentials = basic("user", "password")
        val nextcloudClient = NextcloudClient(url, "user", credentials, context)

        val testList = listOf(
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
