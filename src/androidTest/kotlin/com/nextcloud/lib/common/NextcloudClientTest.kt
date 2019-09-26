/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2019 Tobias Kaminsky
 *   Copyright (C) 2019 Nextcloud GmbH
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

package com.nextcloud.lib.common

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.common.UserInfo
import com.owncloud.android.lib.resources.users.GetUserInfoRemoteOperation
import okhttp3.Credentials
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NextcloudClientTest : AbstractIT() {
    @Test
    fun oneCall() {
        nextcloudClient.credentials = Credentials.basic("user1", "user1")
        call("user1")
    }

    @Test
    fun twoCalls() {
        nextcloudClient.credentials = Credentials.basic("user1", "user1")
        call("user1")
        call("user1")
    }

    @Test
    fun threeCalls() {
        nextcloudClient.credentials = Credentials.basic("user1", "user1")
        call("user1")
        call("user1")
        call("user1")
    }

    @Test
    fun changeCredentialsCalls() {
        nextcloudClient.credentials = Credentials.basic("user1", "user1")
        call("user1")
        nextcloudClient.credentials = Credentials.basic("user2", "user2")
        call("user2")
    }

    private fun call(userId: String) {
        val userInfoResult = GetUserInfoRemoteOperation().execute(nextcloudClient)
        assertTrue(userInfoResult.isSuccess)

        val userInfo = userInfoResult.data[0] as UserInfo
        assertEquals(userId, userInfo.id)
    }
}
