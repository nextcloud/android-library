/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Base64;

public class ManualClientCertificateMaterialTest {
    private byte[] fixture() throws Exception {
        try (InputStream stream = getClass().getResourceAsStream("/certificates/client.p12.base64")) {
            assertNotNull("Missing test-only PKCS#12 fixture", stream);
            byte[] encoded = stream.readAllBytes();
            return Base64.getMimeDecoder().decode(new String(encoded, StandardCharsets.US_ASCII));
        }
    }

    @Test
    public void importExportPreservesPrivateKeyAndCertificateChain() throws Exception {
        byte[] normalized = ManualClientCertificateMaterial.normalize(fixture(), "for-tests-only".toCharArray());
        ManualClientCertificateMaterial.Identity identity = ManualClientCertificateMaterial.read(normalized);
        assertNotNull(identity.privateKey);
        assertEquals("RSA", identity.privateKey.getAlgorithm());
        assertEquals(1, identity.certificateChain().length);
        assertEquals("CN=nextcloud-test-client", identity.certificateChain()[0].getSubjectX500Principal().getName());
    }

    @Test
    public void incorrectPasswordIsRejected() throws Exception {
        try {
            ManualClientCertificateMaterial.normalize(fixture(), "incorrect".toCharArray());
            fail("A wrong PKCS#12 password must not be accepted");
        } catch (GeneralSecurityException | java.io.IOException expected) {
            // Expected.
        }
    }

    @Test
    public void fileWithoutPrivateKeyIsRejected() throws Exception {
        KeyStore empty = KeyStore.getInstance("PKCS12");
        empty.load(null, new char[0]);
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        empty.store(output, new char[0]);
        try {
            ManualClientCertificateMaterial.normalize(output.toByteArray(), new char[0]);
            fail("A PKCS#12 file without a private key must not be accepted");
        } catch (GeneralSecurityException expected) {
            // Expected.
        }
    }
}
