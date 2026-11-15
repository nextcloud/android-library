/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.network;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.AtomicFile;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Encrypted, opt-in client certificates scoped to one TLS hostname and port. */
public final class ManualClientCertificateStore {
    public static final String ALIAS_PREFIX = "MANUAL_";
    private static final String KEY_ALIAS = "NextcloudImportedClientCertificatesV1";
    private static final int MAX_PKCS12_BYTES = 2 * 1024 * 1024;
    private static final int GCM_IV_LENGTH = 12;
    private static final byte FORMAT_VERSION = 1;
    private static final ConcurrentHashMap<String, ManualClientCertificateMaterial.Identity> CACHE =
            new ConcurrentHashMap<>();

    private ManualClientCertificateStore() { }

    private static String serverKey(@NonNull String hostname, int port) {
        String host = hostname.toLowerCase(Locale.ROOT);
        if (host.isEmpty() || host.contains("/") || host.contains("\\") || host.contains("@") ||
                host.contains("#") || Character.isWhitespace(host.charAt(0)) || port < 1 || port > 65535) {
            throw new IllegalArgumentException("Invalid TLS hostname or port");
        }
        return host + ":" + port;
    }

    public static String aliasFor(@NonNull String hostname, int port) {
        String value = serverKey(hostname, port);
        return ALIAS_PREFIX + Base64.encodeToString(value.getBytes(StandardCharsets.UTF_8),
                Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    }

    private static String serverFromAlias(@NonNull String alias) {
        if (!alias.startsWith(ALIAS_PREFIX)) {
            throw new IllegalArgumentException("Not a manual certificate alias");
        }
        String decoded = new String(Base64.decode(alias.substring(ALIAS_PREFIX.length()),
                Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING), StandardCharsets.UTF_8);
        int separator = decoded.lastIndexOf(':');
        if (separator <= 0 || separator == decoded.length() - 1) {
            throw new IllegalArgumentException("Invalid manual certificate alias");
        }
        String hostname = decoded.substring(0, separator);
        int port = Integer.parseInt(decoded.substring(separator + 1));
        String canonical = serverKey(hostname, port);
        if (!canonical.equals(decoded)) {
            throw new IllegalArgumentException("Noncanonical manual certificate alias");
        }
        return canonical;
    }

    private static File file(@NonNull Context context, @NonNull String server)
            throws GeneralSecurityException {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(server.getBytes(StandardCharsets.UTF_8));
        String name = Base64.encodeToString(digest, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
        return new File(context.getNoBackupFilesDir(), "mtls-" + name + ".enc");
    }

    public static boolean isConfigured(@NonNull Context context, @NonNull String hostname, int port) {
        try {
            return file(context, serverKey(hostname, port)).exists();
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            return false;
        }
    }

    public static synchronized void importCertificate(@NonNull Context context, @NonNull String hostname,
                                                       int port, @NonNull byte[] contents,
                                                       @NonNull char[] password)
            throws GeneralSecurityException, IOException {
        if (contents.length == 0 || contents.length > MAX_PKCS12_BYTES) {
            throw new GeneralSecurityException("Invalid PKCS#12 file size");
        }
        String server = serverKey(hostname, port);
        byte[] normalized = ManualClientCertificateMaterial.normalize(contents, password);
        try {
            ManualClientCertificateMaterial.Identity identity = ManualClientCertificateMaterial.read(normalized);
            byte[] encrypted = encrypt(normalized);
            try {
                AtomicFile target = new AtomicFile(file(context, server));
                FileOutputStream stream = target.startWrite();
                try {
                    stream.write(encrypted);
                    target.finishWrite(stream);
                } catch (IOException | RuntimeException e) {
                    target.failWrite(stream);
                    throw e;
                }
            } finally {
                Arrays.fill(encrypted, (byte) 0);
            }
            CACHE.put(server, identity);
        } finally {
            Arrays.fill(normalized, (byte) 0);
        }
    }

    public static synchronized void remove(@NonNull Context context, @NonNull String hostname, int port)
            throws GeneralSecurityException {
        String server = serverKey(hostname, port);
        AtomicFile target = new AtomicFile(file(context, server));
        target.delete();
        CACHE.remove(server);
    }

    @Nullable
    public static PrivateKey getPrivateKey(@NonNull Context context, @NonNull String alias)
            throws GeneralSecurityException, IOException {
        ManualClientCertificateMaterial.Identity identity = identity(context, serverFromAlias(alias));
        return identity == null ? null : identity.privateKey;
    }

    @Nullable
    public static X509Certificate[] getCertificateChain(@NonNull Context context, @NonNull String alias)
            throws GeneralSecurityException, IOException {
        ManualClientCertificateMaterial.Identity identity = identity(context, serverFromAlias(alias));
        return identity == null ? null : identity.certificateChain();
    }

    @Nullable
    private static synchronized ManualClientCertificateMaterial.Identity identity(Context context, String server)
            throws GeneralSecurityException, IOException {
        if (CACHE.containsKey(server)) {
            return CACHE.get(server);
        }
        AtomicFile source = new AtomicFile(file(context, server));
        if (!source.getBaseFile().exists()) {
            return null;
        }
        byte[] encrypted = source.readFully();
        byte[] normalized;
        try {
            normalized = decrypt(encrypted);
        } finally {
            Arrays.fill(encrypted, (byte) 0);
        }
        try {
            ManualClientCertificateMaterial.Identity result = ManualClientCertificateMaterial.read(normalized);
            CACHE.put(server, result);
            return result;
        } finally {
            Arrays.fill(normalized, (byte) 0);
        }
    }

    private static SecretKey aesKey() throws GeneralSecurityException, IOException {
        KeyStore keystore = KeyStore.getInstance("AndroidKeyStore");
        keystore.load(null);
        Key existing = keystore.getKey(KEY_ALIAS, null);
        if (existing instanceof SecretKey) {
            return (SecretKey) existing;
        }
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setKeySize(256)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build());
        return generator.generateKey();
    }

    private static byte[] encrypt(byte[] plaintext) throws GeneralSecurityException, IOException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey(), new SecureRandom());
        byte[] iv = cipher.getIV();
        if (iv.length != GCM_IV_LENGTH) {
            throw new GeneralSecurityException("Unexpected AES-GCM IV length");
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(FORMAT_VERSION);
        output.write(iv, 0, iv.length);
        byte[] ciphertext = cipher.doFinal(plaintext);
        output.write(ciphertext, 0, ciphertext.length);
        return output.toByteArray();
    }

    private static byte[] decrypt(byte[] encrypted) throws GeneralSecurityException, IOException {
        if (encrypted.length < 1 + GCM_IV_LENGTH + 16 || encrypted[0] != FORMAT_VERSION) {
            throw new GeneralSecurityException("Invalid encrypted certificate format");
        }
        byte[] iv = Arrays.copyOfRange(encrypted, 1, 1 + GCM_IV_LENGTH);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey(), new GCMParameterSpec(128, iv));
        return cipher.doFinal(encrypted, 1 + GCM_IV_LENGTH,
                encrypted.length - (1 + GCM_IV_LENGTH));
    }
}
