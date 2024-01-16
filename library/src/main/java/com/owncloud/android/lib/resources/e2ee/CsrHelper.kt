/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2023 Tobias Kaminsky
 *   Copyright (C) 2023 Nextcloud GmbH
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

import android.util.Base64
import androidx.annotation.VisibleForTesting
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.ExtensionsGenerator
import org.bouncycastle.crypto.util.PrivateKeyFactory
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder
import org.bouncycastle.operator.OperatorCreationException
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder
import java.io.IOException
import java.security.KeyPair

/**
 * copied & modified from:
 * https://github.com/awslabs/aws-sdk-android-samples/blob/master/CreateIotCertWithCSR/src/com/amazonaws/demo/csrcert/CsrHelper.java
 * accessed at 31.08.17
 * Original parts are licensed under the Apache License, Version 2.0: http://aws.amazon.com/apache2.0
 * Own parts are licensed under GPLv3+.
 */
class CsrHelper {
    /**
     * Generate CSR with PEM encoding
     *
     * @param keyPair the KeyPair with private and public keys
     * @param userId  userId of CSR owner
     * @return PEM encoded CSR string
     * @throws IOException               thrown if key cannot be created
     * @throws OperatorCreationException thrown if contentSigner cannot be build
     */
    @Throws(IOException::class, OperatorCreationException::class)
    fun generateCsrPemEncodedString(
        keyPair: KeyPair,
        userId: String
    ): String {
        val csr = generateCSR(keyPair, userId)
        val derCSR = csr.encoded
        return "-----BEGIN CERTIFICATE REQUEST-----\n" +
            Base64.encodeToString(
                derCSR,
                Base64.NO_WRAP
            ) + "\n-----END CERTIFICATE REQUEST-----"
    }

    /**
     * Create the certificate signing request (CSR) from private and public keys
     *
     * @param keyPair the KeyPair with private and public keys
     * @param userId  userId of CSR owner
     * @return PKCS10CertificationRequest with the certificate signing request (CSR) data
     * @throws IOException               thrown if key cannot be created
     * @throws OperatorCreationException thrown if contentSigner cannot be build
     */
    @VisibleForTesting
    @Throws(IOException::class, OperatorCreationException::class)
    private fun generateCSR(
        keyPair: KeyPair,
        userId: String
    ): PKCS10CertificationRequest {
        val principal = "CN=$userId"
        val privateKey = PrivateKeyFactory.createKey(keyPair.private.encoded)
        val signatureAlgorithm = DefaultSignatureAlgorithmIdentifierFinder().find("SHA1WITHRSA")
        val digestAlgorithm = DefaultDigestAlgorithmIdentifierFinder().find("SHA-1")
        val signer =
            BcRSAContentSignerBuilder(signatureAlgorithm, digestAlgorithm).build(privateKey)
        val csrBuilder: PKCS10CertificationRequestBuilder =
            JcaPKCS10CertificationRequestBuilder(
                X500Name(principal),
                keyPair.public
            )
        val extensionsGenerator = ExtensionsGenerator()
        extensionsGenerator.addExtension(Extension.basicConstraints, true, BasicConstraints(true))
        csrBuilder.addAttribute(
            PKCSObjectIdentifiers.pkcs_9_at_extensionRequest,
            extensionsGenerator.generate()
        )
        return csrBuilder.build(signer)
    }
}
