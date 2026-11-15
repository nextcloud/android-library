/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.util.Base64;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Small device test for the AndroidKeyStore-backed certificate import. */
@RunWith(AndroidJUnit4.class)
public class ManualClientCertificateStoreIT {
    @Test
    public void importedCertificateIsAvailableOnlyToItsServerAndCanBeRemoved() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String host = "mtls-instrumentation.example";
        String otherHost = "other-instrumentation.example";
        int port = 443;
        byte[] encoded;
        try (InputStream stream = InstrumentationRegistry.getInstrumentation().getContext()
                .getAssets().open("client.p12.base64");
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = stream.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
            encoded = output.toByteArray();
        }
        byte[] certificate = Base64.decode(new String(encoded, StandardCharsets.US_ASCII), Base64.DEFAULT);
        Arrays.fill(encoded, (byte) 0);
        char[] password = "for-tests-only".toCharArray();

        try {
            ManualClientCertificateStore.remove(context, host, port);
            assertFalse(ManualClientCertificateStore.isConfigured(context, host, port));

            ManualClientCertificateStore.importCertificate(context, host, port, certificate, password);

            assertTrue(ManualClientCertificateStore.isConfigured(context, host, port));
            String alias = ManualClientCertificateStore.aliasFor(host, port);
            assertNotNull(ManualClientCertificateStore.getPrivateKey(context, alias));
            assertEquals(1, ManualClientCertificateStore.getCertificateChain(context, alias).length);
            assertFalse(ManualClientCertificateStore.isConfigured(context, otherHost, port));
            assertNull(ManualClientCertificateStore.getPrivateKey(context,
                    ManualClientCertificateStore.aliasFor(otherHost, port)));
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(certificate, (byte) 0);
            ManualClientCertificateStore.remove(context, host, port);
        }

        assertFalse(ManualClientCertificateStore.isConfigured(context, host, port));
    }
}
