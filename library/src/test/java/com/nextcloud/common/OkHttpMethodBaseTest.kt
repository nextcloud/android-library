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
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.Call
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
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
        nextcloudClient = NextcloudClient(uri, userId, credentials, okHttpClient, context)
    }

    @Test
    fun `exceptions throws by OkHttpMethodBase are handled`() {
        // GIVEN
        //      failing method
        val method =
            object : OkHttpMethodBase("http://example.com", true) {
                override fun applyType(temp: Request.Builder) {
                    temp.get()
                }
            }
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
