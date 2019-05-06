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

import java.util.ArrayList;

/**
 * Common base for all integration tests
 */

@RunWith(AndroidJUnit4.class)
public abstract class AbstractIT {
    protected static OwnCloudClient client;
    protected static String userId;

    @BeforeClass
    public static void beforeAll() {
        Bundle arguments = InstrumentationRegistry.getArguments();
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Uri url = Uri.parse(arguments.getString("TEST_SERVER_URL"));
        userId = arguments.getString("TEST_SERVER_USERNAME");
        String password = arguments.getString("TEST_SERVER_PASSWORD");

        client = OwnCloudClientFactory.createOwnCloudClient(url, context, true);
        client.setCredentials(new OwnCloudBasicCredentials(userId, password));
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
