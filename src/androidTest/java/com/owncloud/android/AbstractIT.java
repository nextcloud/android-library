/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2019 Tobias Kaminsky
 *   Copyright (C) 2019 Nextcloud GmbH
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
package com.owncloud.android;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.nextcloud.common.NextcloudClient;
import com.owncloud.android.lib.common.OwnCloudBasicCredentials;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory;
import com.owncloud.android.lib.common.network.CertificateCombinedException;
import com.owncloud.android.lib.common.network.NetworkUtils;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.e2ee.ToggleEncryptionRemoteOperation;
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.RemoveFileRemoteOperation;
import com.owncloud.android.lib.resources.files.model.RemoteFile;
import com.owncloud.android.lib.resources.status.GetStatusRemoteOperation;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import androidx.test.platform.app.InstrumentationRegistry;
import okhttp3.Credentials;

import static junit.framework.TestCase.assertTrue;

/**
 * Common base for all integration tests
 */

public abstract class AbstractIT {
    @Rule
    public RetryTestRule retryTestRule = new RetryTestRule();
    
    private static final int BUFFER_SIZE = 1024;

    public static OwnCloudClient client;
    protected static NextcloudClient nextcloudClient;
    protected static Context context;

    protected String baseFolderPath = "/test_for_build/";

    public static final String ASSETS__TEXT_FILE_NAME = "textFile.txt";
    private static String LOCAL_TRUSTSTORE_FILENAME = "knownServers.bks";

    @BeforeClass
    public static void beforeAll() throws InterruptedException,
            CertificateException,
            NoSuchAlgorithmException,
            KeyStoreException,
            IOException {
        Bundle arguments = InstrumentationRegistry.getArguments();
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Uri url = Uri.parse(arguments.getString("TEST_SERVER_URL"));
        String loginName = arguments.getString("TEST_SERVER_USERNAME");
        String password = arguments.getString("TEST_SERVER_PASSWORD");

        client = OwnCloudClientFactory.createOwnCloudClient(url, context, true);
        client.setCredentials(new OwnCloudBasicCredentials(loginName, password));
        client.setUserId(loginName); // for test same as userId

        OwnCloudClientManagerFactory.setUserAgent("Mozilla/5.0 (Android) Nextcloud-android/1.0.0");

        String userId = loginName; // for test same as userId
        String credentials = Credentials.basic(loginName, password);
        nextcloudClient = new NextcloudClient(url, userId, credentials, context);

        waitForServer(client, url);
        testConnection();
    }

    private static void waitForServer(OwnCloudClient client, Uri baseUrl) {
        // use http 
        Uri httpUrl = Uri.parse(baseUrl.toString().replaceFirst("https", "http"));
        GetMethod get = new GetMethod(httpUrl + "/status.php");

        try {
            int i = 0;
            while (client.executeMethod(get) != HttpStatus.SC_OK && i < 3) {
                System.out.println("wait…");
                Thread.sleep(60 * 1000);
                i++;
            }

            if (i == 3) {
                Assert.fail("Server not ready!");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        RemoteOperationResult result = new ReadFolderRemoteOperation("/").execute(client);
        assertTrue(result.getLogMessage(), result.isSuccess());

        for (Object object : result.getData()) {
            RemoteFile remoteFile = (RemoteFile) object;

            if (!"/".equals(remoteFile.getRemotePath())) {
                if (remoteFile.isEncrypted()) {
                    Assert.assertTrue(new ToggleEncryptionRemoteOperation(
                            remoteFile.getLocalId(),
                            remoteFile.getRemotePath(),
                            false)
                                              .execute(client)
                                              .isSuccess());
                }
                
                assertTrue(new RemoveFileRemoteOperation(remoteFile.getRemotePath())
                        .execute(client).isSuccess());
            }
        }

        // delete keystore
        new File(context.getFilesDir(), LOCAL_TRUSTSTORE_FILENAME).delete();
    }

    public static File getFile(String filename) throws IOException {
        InputStream inputStream = context.getAssets().open(filename);
        File temp = File.createTempFile("file", "file");
        FileUtils.copyInputStreamToFile(inputStream, temp);

        return temp;
    }
}
