/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2024 Tobias Kaminsky
 *   Copyright (C) 2024 Nextcloud GmbH
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
        val urlEncoded: String = CsrHelper().generateCsrPemEncodedString(keyPair, client.userId)

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
