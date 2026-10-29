/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.e2ee

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory
import com.owncloud.android.lib.resources.users.GetServerPublicKeyRemoteOperation
import org.apache.commons.httpclient.HttpStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetServerPublicKeyRemoteOperationIT : AbstractIT() {
    @Before
    fun init() {
        OwnCloudClientManagerFactory.setUserAgent(SUPPORTED_USER_AGENT)
    }

    @Test
    fun serverPublicKeyIsReturnedAsPem() {
        val result = GetServerPublicKeyRemoteOperation().execute(nextcloudClient)

        assertTrue(result.isSuccess)
        val key = result.resultData.trim()
        assertTrue(key.startsWith(PEM_HEADER))
        assertTrue(key.endsWith(PEM_FOOTER))
    }

    @Test
    fun unsupportedClientIsRejected() {
        OwnCloudClientManagerFactory.setUserAgent(UNSUPPORTED_USER_AGENT)

        val result = GetServerPublicKeyRemoteOperation().execute(nextcloudClient)

        assertFalse(result.isSuccess)
        assertEquals(HttpStatus.SC_FORBIDDEN, result.httpCode)
    }

    companion object {
        private const val SUPPORTED_USER_AGENT = "Mozilla/5.0 (Android) Nextcloud-android/3.13.0"
        private const val UNSUPPORTED_USER_AGENT = "Mozilla/5.0 (Android) Nextcloud-android/1.0.0"
        private const val PEM_HEADER = "-----BEGIN PUBLIC KEY-----"
        private const val PEM_FOOTER = "-----END PUBLIC KEY-----"
    }
}
