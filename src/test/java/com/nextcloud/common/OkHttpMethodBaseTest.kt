/**
 * Nextcloud Android Library is available under MIT license
 *
 * @author Chris Narkiewicz <hello@ezaquarii.com>
 * Copyright (C) 2019 Chris Narkiewicz
 * Copyright (C) 2019 Nextcloud GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.nextcloud.common

import android.content.Context
import android.net.Uri
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.Call
import okhttp3.Credentials
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.io.IOException

class OkHttpMethodBaseTest {

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var uri: Uri

    @Mock
    lateinit var okHttpClient: OkHttpClient

    lateinit var nextcloudClient: NextcloudClient

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val userId = "test"
        val credentials = Credentials.basic("username", "password")
        nextcloudClient = NextcloudClient(uri, userId, credentials, okHttpClient)
    }

    @Test
    fun `exceptions throws by OkHttpMethodBase are handled`() {
        // GIVEN
        //      failing method
        val method = object : OkHttpMethodBase("http://example.com", true) {}
        val call = mock<Call>()
        whenever(okHttpClient.newCall(any())).thenReturn(call)
        whenever(call.execute()).thenThrow(IOException::class.java)

        // WHEN
        //      method is called
        val code = method.execute(nextcloudClient)

        // THEN
        //      okhttp call was executed
        //      exception is not propagated
        //      error code is returned instead
        verify(call).execute()
        assertEquals(OkHttpMethodBase.UNKNOWN_STATUS_CODE, code)
    }
}
