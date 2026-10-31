/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.common

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.httpclient.URI
import org.apache.commons.httpclient.methods.GetMethod
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CustomHeadersTest {
    private val headers = mapOf("CF-Access-Client-Id" to "id", "CF-Access-Client-Secret" to "secret")
    private val client = OkHttpClient.Builder().addNetworkInterceptor(CustomHeadersInterceptor).build()

    private lateinit var home: MockWebServer
    private lateinit var foreign: MockWebServer

    @Before
    fun setUp() {
        home = MockWebServer().apply { start() }
        foreign = MockWebServer().apply { start() }
    }

    @After
    fun tearDown() {
        CustomHeaders.clear()
        home.close()
        foreign.close()
    }

    private fun registerHome() =
        assertTrue(CustomHeaders.register(CustomHeaders.originOf("http", home.hostName, home.port)!!, headers))

    @Test
    fun sendsOnlyToRegisteredOrigin() {
        CustomHeaders.register("https://cloud.example.com", headers)

        assertEquals(headers, CustomHeaders.forDestination("https", "cloud.example.com", 443))
        assertEquals(headers, CustomHeaders.forDestination("HTTPS", "CLOUD.example.com", -1))
        assertTrue(CustomHeaders.forDestination("https", "other.example.com", 443).isEmpty())
        assertTrue(CustomHeaders.forDestination("https", "cloud.example.com", 8443).isEmpty())
        assertTrue(CustomHeaders.forDestination("https", "cloud.example.com.evil.net", 443).isEmpty())
        assertTrue(CustomHeaders.forDestination("http", "cloud.example.com", 80).isEmpty())
        assertTrue(CustomHeaders.forDestination(null, "cloud.example.com", 443).isEmpty())
    }

    @Test
    fun originMatchesJavaScriptForm() {
        assertEquals("https://cloud.example.com", CustomHeaders.originOf("HTTPS", "Cloud.Example.com", 443))
        assertEquals("https://cloud.example.com:8443", CustomHeaders.originOf("https", "cloud.example.com", 8443))
        assertNull(CustomHeaders.originOf("https", null, 443))
    }

    @Test
    fun rejectsInvalidHeaders() {
        fun register(
            name: String,
            value: String
        ) = CustomHeaders.register("https://h", mapOf(name to value))

        assertFalse(register("Authorization", "x"))
        assertFalse(register("user-agent", "x"))
        assertFalse(register("Host", "x"))
        assertFalse(register("Bad Name", "x"))
        assertFalse(register("X-Ok", "a\r\nInjected: 1"))
        assertFalse(register("X-Ok", ""))
        assertTrue(CustomHeaders.origins.isEmpty())
        assertTrue(register("X-Ok", "value"))
    }

    @Test
    fun apacheMethodGetsHeadersOnlyForRegisteredOrigin() {
        CustomHeaders.register("https://cloud.example.com", headers)
        val method = GetMethod("https://cloud.example.com/remote.php/dav")

        CustomHeaders.applyTo(method)
        assertEquals("secret", method.getRequestHeader("CF-Access-Client-Secret").value)

        method.uri = URI("https://evil.example.net/x", true)
        CustomHeaders.applyTo(method)
        assertNull(method.getRequestHeader("CF-Access-Client-Secret"))
        assertNull(method.getRequestHeader("CF-Access-Client-Id"))
    }

    @Test
    fun okHttpDoesNotLeakHeadersOnCrossHostRedirect() {
        registerHome()
        foreign.enqueue(MockResponse.Builder().code(200).build())
        home.enqueue(
            MockResponse
                .Builder()
                .code(302)
                .addHeader("Location", foreign.url("/landing").toString())
                .build()
        )

        client.newCall(Request.Builder().url(home.url("/start")).build()).execute().close()

        assertEquals("secret", home.takeRequest().headers["CF-Access-Client-Secret"])
        val leaked = foreign.takeRequest()
        assertNull(leaked.headers["CF-Access-Client-Secret"])
        assertNull(leaked.headers["CF-Access-Client-Id"])
    }

    @Test
    fun okHttpKeepsOperationHeadersForOtherHosts() {
        CustomHeaders.register("https://cloud.example.com", mapOf("OCS-APIREQUEST" to "true", "Accept" to "x"))
        foreign.enqueue(MockResponse.Builder().code(200).build())

        client
            .newCall(
                Request
                    .Builder()
                    .url(foreign.url("/x"))
                    .header("OCS-APIREQUEST", "true")
                    .header("Accept", "a")
                    .build()
            ).execute()
            .close()

        val received = foreign.takeRequest().headers
        assertEquals("true", received["OCS-APIREQUEST"])
        assertEquals("a", received["Accept"])
    }

    @Test
    fun apacheMethodRemovesHeadersAfterRegistrationIsReplaced() {
        CustomHeaders.register("https://cloud.example.com", mapOf("X-Old-Secret" to "old"))
        val method = GetMethod("https://cloud.example.com/remote.php/dav")

        CustomHeaders.applyTo(method)
        assertEquals("old", method.getRequestHeader("X-Old-Secret").value)

        CustomHeaders.register("https://cloud.example.com", mapOf("X-New-Secret" to "new"))
        method.uri = URI("https://evil.example.net/x", true)
        CustomHeaders.applyTo(method)

        assertNull(method.getRequestHeader("X-Old-Secret"))
        assertNull(method.getRequestHeader("X-New-Secret"))
    }

    @Test
    fun apacheMethodKeepsOperationHeaders() {
        CustomHeaders.register("https://cloud.example.com", headers)
        val method = GetMethod("https://other.example.net/x")
        method.setRequestHeader("Accept", "application/json")

        CustomHeaders.applyTo(method)

        assertEquals("application/json", method.getRequestHeader("Accept").value)
    }

    @Test
    fun apacheMethodRestoresOverriddenOperationHeaderOnRedirect() {
        CustomHeaders.register("https://cloud.example.com", mapOf("OCS-APIREQUEST" to "custom"))
        val method = GetMethod("https://cloud.example.com/ocs")
        method.setRequestHeader("OCS-APIREQUEST", "true")

        CustomHeaders.applyTo(method)
        assertEquals("custom", method.getRequestHeader("OCS-APIREQUEST").value)

        method.uri = URI("https://other.example.net/ocs", true)
        CustomHeaders.applyTo(method)
        assertEquals("true", method.getRequestHeader("OCS-APIREQUEST").value)
    }

    @Test
    fun internationalizedHostMatchesPunycode() {
        assertEquals("https://xn--bcher-kva.example", CustomHeaders.originOf("https", "bücher.example", -1))
        CustomHeaders.register("https://xn--bcher-kva.example", headers)

        assertEquals(headers, CustomHeaders.forDestination("https", "xn--bcher-kva.example", 443))
        assertEquals(headers, CustomHeaders.forDestination("https", "BÜCHER.example", 443))
    }

    @Test
    fun unregisterStopsSending() {
        CustomHeaders.register("https://cloud.example.com", headers)
        CustomHeaders.unregister("https://cloud.example.com")

        assertTrue(CustomHeaders.forDestination("https", "cloud.example.com", 443).isEmpty())
    }
}
