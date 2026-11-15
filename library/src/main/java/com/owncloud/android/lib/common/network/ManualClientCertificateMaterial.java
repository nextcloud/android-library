/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

/** A single client identity that can be kept inside an independently encrypted PKCS#12 blob. */
public final class ManualClientCertificateMaterial {
    private static final String ENTRY_ALIAS = "client";
    private static final char[] INTERNAL_PASSWORD = new char[0];

    private ManualClientCertificateMaterial() { }

    public static byte[] normalize(byte[] original, char[] password)
            throws GeneralSecurityException, IOException {
        KeyStore source = KeyStore.getInstance("PKCS12");
        source.load(new ByteArrayInputStream(original), password);
        KeyStore.PrivateKeyEntry found = null;

        Enumeration<String> aliases = source.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (!source.isKeyEntry(alias)) {
                continue;
            }
            if (found != null) {
                throw new GeneralSecurityException("Only one private key per file is supported");
            }
            KeyStore.Entry entry = source.getEntry(alias, new KeyStore.PasswordProtection(password));
            if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
                throw new GeneralSecurityException("Client certificate does not contain a private key");
            }
            found = (KeyStore.PrivateKeyEntry) entry;
        }
        if (found == null) {
            throw new GeneralSecurityException("No private key in the selected PKCS#12 file");
        }
        Certificate[] chain = found.getCertificateChain();
        if (chain == null || chain.length == 0 || !(chain[0] instanceof X509Certificate)) {
            throw new GeneralSecurityException("No X.509 certificate chain in PKCS#12 file");
        }
        ((X509Certificate) chain[0]).checkValidity();

        KeyStore normalized = KeyStore.getInstance("PKCS12");
        normalized.load(null, INTERNAL_PASSWORD);
        normalized.setKeyEntry(ENTRY_ALIAS, found.getPrivateKey(), INTERNAL_PASSWORD, chain);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        normalized.store(output, INTERNAL_PASSWORD);
        return output.toByteArray();
    }

    public static Identity read(byte[] normalized) throws GeneralSecurityException, IOException {
        KeyStore store = KeyStore.getInstance("PKCS12");
        store.load(new ByteArrayInputStream(normalized), INTERNAL_PASSWORD);
        KeyStore.Entry entry = store.getEntry(ENTRY_ALIAS, new KeyStore.PasswordProtection(INTERNAL_PASSWORD));
        if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
            throw new GeneralSecurityException("Stored client certificate is missing its private key");
        }
        KeyStore.PrivateKeyEntry identity = (KeyStore.PrivateKeyEntry) entry;
        Certificate[] chain = identity.getCertificateChain();
        X509Certificate[] certificates = new X509Certificate[chain.length];
        for (int i = 0; i < chain.length; i++) {
            if (!(chain[i] instanceof X509Certificate)) {
                throw new GeneralSecurityException("Non-X.509 certificate in stored chain");
            }
            certificates[i] = (X509Certificate) chain[i];
        }
        certificates[0].checkValidity();
        return new Identity(identity.getPrivateKey(), certificates);
    }

    public static final class Identity {
        public final PrivateKey privateKey;
        private final X509Certificate[] chain;

        private Identity(PrivateKey privateKey, X509Certificate[] chain) {
            this.privateKey = privateKey;
            this.chain = chain.clone();
        }

        public X509Certificate[] certificateChain() {
            return chain.clone();
        }
    }
}
