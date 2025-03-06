/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023-2024 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.e2ee

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory
import com.owncloud.android.lib.resources.users.GetPublicKeyRemoteOperation
import com.owncloud.android.lib.resources.users.SendCSRRemoteOperation
import junit.framework.TestCase
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import java.security.KeyPairGenerator
import java.security.SecureRandom

class SendCSRRemoteOperationIT : AbstractIT() {
    @Before
    fun init() {
        // E2E server app checks for official NC client with >=3.13.0,
        // and blocks all other clients, e.g. 3rd party apps using this lib
        OwnCloudClientManagerFactory.setUserAgent("Mozilla/5.0 (Android) Nextcloud-android/3.13.0")
    }

    @Throws(Throwable::class)
    @Test
    fun publicKey() {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(KEY_SIZE, SecureRandom())

        val keyPair = keyGen.genKeyPair()

        // create CSR
        val urlEncoded: String =
            CsrHelper().generateCsrPemEncodedString(keyPair, client.userId, SignatureAlgorithm.SHA256)

        val operation = SendCSRRemoteOperation(urlEncoded)
        var result = operation.execute(nextcloudClient)

        assertTrue(result.isSuccess)

        // verify public key
        result = GetPublicKeyRemoteOperation(client.userId).execute(nextcloudClient)
        assertTrue(result.isSuccess)
        TestCase.assertNotNull(result.resultData)
    }

    companion object {
        const val KEY_SIZE = 2048
    }
}
