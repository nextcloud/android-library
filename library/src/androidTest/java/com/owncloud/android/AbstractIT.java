/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2018-2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2025 TSI-mc <surinder.kumar@t-systems.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.test.platform.app.InstrumentationRegistry;

import com.nextcloud.android.lib.resources.files.ToggleFileLockRemoteOperation;
import com.nextcloud.common.NextcloudClient;
import com.owncloud.android.lib.common.OwnCloudBasicCredentials;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory;
import com.owncloud.android.lib.common.network.CertificateCombinedException;
import com.owncloud.android.lib.common.network.NetworkUtils;
import com.owncloud.android.lib.common.network.WebdavEntry;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.e2ee.ToggleEncryptionRemoteOperation;
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.RemoveFileRemoteOperation;
import com.owncloud.android.lib.resources.files.model.RemoteFile;
import com.owncloud.android.lib.resources.status.GetCapabilitiesRemoteOperation;
import com.owncloud.android.lib.resources.status.GetStatusRemoteOperation;
import com.owncloud.android.lib.resources.status.OCCapability;
import com.owncloud.android.lib.resources.status.OwnCloudVersion;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import okhttp3.Credentials;

/**
 * Common base for all integration tests
 */
public abstract class AbstractIT {
    @Rule
    public RetryTestRule retryTestRule = new RetryTestRule();

    private static final int BUFFER_SIZE = 1024;
    public static final long RANDOM_MTIME = 1464818400L;
    public static final int MILLI_TO_SECOND = 1000;

    public static OwnCloudClient client;
    public static OwnCloudClient client2;
    protected static NextcloudClient nextcloudClient;
    protected static Context context;
    protected static Uri url;

    protected String baseFolderPath = "/test_for_build/";

    public static final String ASSETS__TEXT_FILE_NAME = "textFile.txt";
    private static final String LOCAL_TRUSTSTORE_FILENAME = "knownServers.bks";
    private static final String TAG = "AbstractIT";

    @BeforeClass
    public static void beforeAll() throws InterruptedException,
        CertificateException,
        NoSuchAlgorithmException,
        KeyStoreException,
        IOException {
        Bundle arguments = InstrumentationRegistry.getArguments();
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        url = Uri.parse(arguments.getString("TEST_SERVER_URL"));
        String loginName = arguments.getString("TEST_SERVER_USERNAME");
        String password = arguments.getString("TEST_SERVER_PASSWORD");

        client = OwnCloudClientFactory.createOwnCloudClient(url, context, true);
        client.setCredentials(new OwnCloudBasicCredentials(loginName, password));
        client.setUserId(loginName); // for test same as userId

        // second user to test internal sharing
        String loginName2 = arguments.getString("TEST_SERVER_USERNAME2");
        String password2 = arguments.getString("TEST_SERVER_PASSWORD2");

        client2 = OwnCloudClientFactory.createOwnCloudClient(url, context, true);
        client2.setCredentials(new OwnCloudBasicCredentials(loginName2, password2));
        client2.setUserId(loginName2); // for test same as userId

        OwnCloudClientManagerFactory.setUserAgent("Mozilla/5.0 (Android) Nextcloud-android/1.0.0");

        String userId = loginName; // for test same as userId
        String credentials = Credentials.basic(loginName, password);
        nextcloudClient = new NextcloudClient(url, userId, credentials, context);

        waitForServer(client, url);
        testConnection();
    }

    private static void waitForServer(OwnCloudClient client, Uri baseUrl) {
        String statusUrl = baseUrl + "/status.php";
        GetMethod get;
        int maxRetries = 3;

        for (int i = 0; i < maxRetries; i++) {
            get = new GetMethod(statusUrl);

            try {
                if (client.executeMethod(get) == HttpStatus.SC_OK) {
                    Log_OC.d(TAG, "Server is ready");
                    return;
                }

                Log_OC.d(TAG, "Server not ready, retrying in 60 seconds...");
                TimeUnit.MINUTES.sleep(1);
            } catch (Exception e) {
                Log_OC.d(TAG, "Server not ready, failed: " + e);
            } finally {
                get.releaseConnection();
            }
        }
    }

    private static void testConnection() throws KeyStoreException,
        CertificateException,
        NoSuchAlgorithmException,
        IOException,
        InterruptedException {
        GetStatusRemoteOperation getStatus = new GetStatusRemoteOperation(context);

        RemoteOperationResult result = getStatus.execute(client);

        if (result.isSuccess()) {
            Log_OC.d("AbstractIT", "Connection to server successful");
        } else {
            if (RemoteOperationResult.ResultCode.SSL_RECOVERABLE_PEER_UNVERIFIED.equals(result.getCode())) {
                Log_OC.d("AbstractIT", "Accepting certificate");

                CertificateCombinedException exception = (CertificateCombinedException) result.getException();
                X509Certificate certificate = exception.getServerCertificate();

                NetworkUtils.addCertToKnownServersStore(certificate, context);
                Thread.sleep(1000);

                assertEquals(certificate,
                    NetworkUtils.getKnownServersStore(context)
                        .getCertificate(Integer.toString(certificate.hashCode()))
                );

                // retry
                getStatus = new GetStatusRemoteOperation(context);
                result = getStatus.execute(client);

                if (!result.isSuccess()) {
                    throw new RuntimeException("No connection to server possible, even with accepted cert");
                }

            } else {
                throw new RuntimeException("No connection to server possible: " + result.getCode());
            }
        }
    }

    public String createFile(String name) throws IOException {
        File tempDir = context.getExternalCacheDir();

        if (tempDir == null) {
            throw new IOException("Temp dir is null");
        }

        if (!tempDir.exists()) {
            if (!tempDir.mkdirs()) {
                throw new IOException("Cannot create temp dir: " + tempDir.getAbsolutePath());
            }
        }

        File file = new File(tempDir + File.separator + name);

        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Cannot create file: " + file.getAbsolutePath());
        }

        assertTrue(file.exists());

        return file.getAbsolutePath();
    }

    public String createFile(String name, int iteration) throws IOException {
        File tempDir = context.getExternalCacheDir();

        if (tempDir == null) {
            throw new IOException("Temp dir is null");
        }

        if (!tempDir.exists()) {
            if (!tempDir.mkdirs()) {
                throw new IOException("Cannot create temp dir: " + tempDir.getAbsolutePath());
            }
        }

        File file = new File(tempDir + File.separator + name);

        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Cannot create file: " + file.getAbsolutePath());
        }

        assertTrue(file.exists());

        FileWriter writer = new FileWriter(file);

        for (int i = 0; i < iteration; i++) {
            writer.write("123123123123123123123123123\n");
        }
        writer.flush();
        writer.close();

        return file.getAbsolutePath();
    }

    /**
     * Extracts file from AssetManager to cache folder.
     *
     * @param fileName Name of the asset file to extract.
     * @param context  Android context to access assets and file system.
     * @return File instance of the extracted file.
     */
    public static File extractAsset(String fileName, Context context) throws IOException {
        File extractedFile = new File(context.getCacheDir() + File.separator + fileName);
        if (!extractedFile.exists()) {
            InputStream in = context.getAssets().open(fileName);
            FileOutputStream out = new FileOutputStream(extractedFile);
            byte[] buffer = new byte[BUFFER_SIZE];
            int readCount;
            while ((readCount = in.read(buffer)) != -1) {
                out.write(buffer, 0, readCount);
            }
            out.flush();
            out.close();
            in.close();
        }
        return extractedFile;
    }

    @After
    public void after() {
        removeOnClient(client);
        removeOnClient(client2);
    }

    private void removeOnClient(OwnCloudClient client) {
        final var result = new ReadFolderRemoteOperation("/").execute(client);
        assertTrue(result.getLogMessage(context), result.isSuccess());

        for (Object object : result.getData()) {
            if (!(object instanceof RemoteFile remoteFile)) {
                Log_OC.d(TAG, "Skipping removeOnClient: not instance of RemoteFile");
                continue;
            }

            String remotePath = remoteFile.getRemotePath();

            if ("/".equals(remotePath) || remoteFile.getMountType() == WebdavEntry.MountType.GROUP) {
                Log_OC.d(TAG, "Skipping removeOnClient: remote path is root path or mount type is group");
                continue;
            }

            if (remoteFile.isEncrypted()) {
                assertTrue(toggleEncryptionRemoteFile(remoteFile));
            }

            if (remoteFile.isLocked() && remotePath != null) {
                unlockRemoteFile(remotePath);
            }

            boolean isRemoteFileRemoved = removeRemoteFile(remotePath);
            final var removeFileOperationErrorMessage = ("Failed to remove " + remotePath);
            assertTrue(removeFileOperationErrorMessage, isRemoteFileRemoved);
        }

        boolean isKeyStoreDeleted = new File(context.getFilesDir(), LOCAL_TRUSTSTORE_FILENAME).delete();
        Log_OC.d(TAG, "KeyStore file deletion result: " + isKeyStoreDeleted);
    }

    private boolean toggleEncryptionRemoteFile(RemoteFile remoteFile) {
        final var operation = new ToggleEncryptionRemoteOperation(remoteFile.getLocalId(), remoteFile.getRemotePath(), false);
        final var result = operation.execute(client);
        return result.isSuccess();
    }

    private void unlockRemoteFile(String path) {
        final var operation = new ToggleFileLockRemoteOperation(false, path);
        final var result = operation.execute(nextcloudClient);
        if (result.isSuccess()) {
            Log_OC.d(TAG, "Locked file: " + path + " unlocked");
        }
    }

    private boolean removeRemoteFile(String path) {
        final var operation = new RemoveFileRemoteOperation(path);
        final var result = operation.execute(client);
        return result.isSuccess();
    }

    public static File getFile(String filename) throws IOException {
        InputStream inputStream = context.getAssets().open(filename);
        File temp = File.createTempFile("file", "file");
        FileUtils.copyInputStreamToFile(inputStream, temp);

        return temp;
    }

    protected void shortSleep() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void longSleep() {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void testOnlyOnServer(OwnCloudVersion version) {
        OCCapability ocCapability = (OCCapability) new GetCapabilitiesRemoteOperation()
            .execute(nextcloudClient)
            .getSingleData();
        assumeTrue(ocCapability.getVersion().isNewerOrEqual(version));
    }
}
