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
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import okhttp3.*
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.io.IOException

class NextcloudClientTest {

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
        val credentials = Credentials.basic("login", "test")
        nextcloudClient = NextcloudClient(uri, userId, credentials, okHttpClient)
    }

    @Test
    fun `exceptions handled when RemoteOperations is executed`() {
        // GIVEN
        //      failing operations
        //      operations throws any kind of exception
        val exception = RuntimeException("test exception")
        val operation = object : RemoteOperation() {
            override fun run(client: NextcloudClient?): RemoteOperationResult {
                throw exception
            }
        }

        // WHEN
        //      operation is executed
        val response = nextcloudClient.execute(operation)

        // THEN
        //      exception is not propagated
        //      error result is returned
        assertSame("Exception should be returned", exception, response.exception)
    }

    @Test
    fun `exceptions raised by okhttp are returned`() {
        // GIVEN
        //      failing okhttp request
        val request = mock<Request>()
        val call = mock<Call>()
        whenever(okHttpClient.newCall(request)).thenReturn(call)
        val expectedException = IOException()
        whenever(call.execute()).thenThrow(expectedException)

        // WHEN
        //      request is executed
        val result = nextcloudClient.execute(request)

        // THEN
        //      okhttp call is executed and throws
        //      exception is caught internally
        //      exception is returned
        verify(call).execute()
        assertNull(result.result)
        assertSame(expectedException, result.error)
    }

    @Test
    fun `result returned by okhttp are returned`() {
        // GIVEN
        //      okhttp request
        val request = mock<Request>()
        val okHttpResponse = mock<Response>()
        val call = mock<Call>()
        whenever(okHttpClient.newCall(request)).thenReturn(call)
        whenever(call.execute()).thenReturn(okHttpResponse)

        // WHEN
        //      request is executed
        val result = nextcloudClient.execute(request)

        // THEN
        //      okhttp call is executed and throws
        //      exception is caught internally
        //      exception is returned
        verify(call).execute()
        assertSame(okHttpResponse, result.result)
        assertNull(result.error)
    }
}
