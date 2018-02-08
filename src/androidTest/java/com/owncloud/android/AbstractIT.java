package com.owncloud.android;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.owncloud.android.lib.common.OwnCloudBasicCredentials;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.RemoveFileRemoteOperation;
import com.owncloud.android.lib.resources.files.model.RemoteFile;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static junit.framework.TestCase.assertTrue;

/**
 * Common base for all integration tests
 */

@RunWith(AndroidJUnit4.class)
public abstract class AbstractIT {
    private static final int BUFFER_SIZE = 1024;
    
    protected static OwnCloudClient client;
    protected static Context context;

    @BeforeClass
    public static void beforeAll() {
        Bundle arguments = InstrumentationRegistry.getArguments();
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Uri url = Uri.parse(arguments.getString("TEST_SERVER_URL"));
        String loginName = arguments.getString("TEST_SERVER_USERNAME");
        String password = arguments.getString("TEST_SERVER_PASSWORD");

        client = OwnCloudClientFactory.createOwnCloudClient(url, context, true);
        client.setCredentials(new OwnCloudBasicCredentials(loginName, password));
        client.setUserId(loginName); // for test same as userId
    }

    public String createFile(String name) throws IOException {
        File tempDir = context.getFilesDir();

        File file = new File(tempDir + File.separator + name);
        file.createNewFile();

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
            InputStream in = null;
            FileOutputStream out = null;
            in = context.getAssets().open(fileName);
            out = new FileOutputStream(extractedFile);
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
        ArrayList list = new ReadFolderRemoteOperation("/").execute(client).getData();

        for (Object object : list) {
            RemoteFile remoteFile = (RemoteFile) object;

            if (!remoteFile.getRemotePath().equals("/")) {
                new RemoveFileRemoteOperation(remoteFile.getRemotePath()).execute(client);
            }
        }
    }
}
