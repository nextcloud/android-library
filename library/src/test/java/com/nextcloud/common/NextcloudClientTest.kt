/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019 Chris Narkiewicz <hello@ezaquarii.com>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.common

import android.content.Context
import android.net.Uri
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import okhttp3.Call
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
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
        nextcloudClient = NextcloudClient(uri, userId, credentials, okHttpClient, context)
    }

    @Test
    fun `exceptions handled when RemoteOperations is executed`() {
        // GIVEN
        //      failing operations
        //      operations throws any kind of exception
        val exception = RuntimeException("test exception")
        val operation =
            object : RemoteOperation<String>() {
                override fun run(client: NextcloudClient?): RemoteOperationResult<String> {
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
