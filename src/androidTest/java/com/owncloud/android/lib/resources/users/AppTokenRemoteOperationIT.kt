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
package com.owncloud.android.lib.resources.users

import androidx.test.platform.app.InstrumentationRegistry
import com.owncloud.android.AbstractIT
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import okhttp3.Credentials.basic
import org.junit.Test

class AppTokenRemoteOperationIT : AbstractIT() {
    @Test
    fun createAppPassword() {
        val sut = ConvertAppTokenRemoteOperation()
        val result = sut.execute(nextcloudClient)

        assertTrue(result.isSuccess)

        val arguments = InstrumentationRegistry.getArguments()

        val password: String? = arguments.getString("TEST_SERVER_PASSWORD")
        val newPassword = result.resultData

        assertTrue(newPassword.isNotBlank())
        assertTrue(password != newPassword)
    }

    @Test
    fun checkAppPassword() {
        // first create new app token
        val sut = ConvertAppTokenRemoteOperation()
        val result = sut.execute(nextcloudClient)

        assertTrue(result.isSuccess)

        val arguments = InstrumentationRegistry.getArguments()
        val username: String = arguments.getString("TEST_SERVER_USERNAME", "")
        val password: String = arguments.getString("TEST_SERVER_PASSWORD", "")
        val newPassword = result.resultData

        assertTrue(newPassword.isNotBlank())
        assertTrue(password != newPassword)

        // second use this app token to check
        nextcloudClient.credentials = basic(username, newPassword)
        val result2 = sut.execute(nextcloudClient)

        assertTrue(result2.isSuccess)
        assertTrue(result2.resultData.isEmpty())
    }

    @Test
    fun deleteAppPassword() {
        val arguments = InstrumentationRegistry.getArguments()
        val username: String = arguments.getString("TEST_SERVER_USERNAME", "")
        val password: String = arguments.getString("TEST_SERVER_PASSWORD", "")
        nextcloudClient.credentials = basic(username, password)

        // first: create a new app password
        val createPassword = ConvertAppTokenRemoteOperation()
        val result = createPassword.execute(nextcloudClient)

        assertTrue(result.isSuccess)

        val newPassword = result.resultData

        assertTrue(newPassword.isNotBlank())
        assertTrue(password != newPassword)

        nextcloudClient.credentials = basic(username, newPassword)

        // check that new password works
        assertTrue(createPassword.execute(nextcloudClient).isSuccess)

        // second: delete this new app password
        val sut = DeleteAppPasswordRemoteOperation()
        val result2 = sut.execute(nextcloudClient)

        assertTrue(result2.isSuccess)

        // check if old password can still be used
        assertFalse(createPassword.execute(nextcloudClient).isSuccess)
    }

    override fun after() {
        // do nothing
    }
}
