package com.owncloud.android;

import android.net.Uri;

import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.RemoveFileRemoteOperation;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests related to file operations
 */
@RunWith(AndroidJUnit4.class)
public class FileIT extends AbstractIT {
    @Test
    public void testCreateFolderSuccess() {
        String path = "/testFolder/";

        // create folder
        assertTrue(new CreateFolderRemoteOperation(path, true).execute(client).isSuccess());

        // verify folder
        assertTrue(new ReadFolderRemoteOperation(path).execute(client).isSuccess());

        // remove folder
        assertTrue(new RemoveFileRemoteOperation(path).execute(client).isSuccess());
    }

    @Test
    public void testCreateFolderFailure() {
        String path = "/testFolder/";

        // create folder
        assertTrue(new CreateFolderRemoteOperation(path, true).execute(client).isSuccess());

        // create folder a second time will fail
        assertFalse(new CreateFolderRemoteOperation(path, true).execute(client).isSuccess());

        // remove folder
        assertTrue(new RemoveFileRemoteOperation(path).execute(client).isSuccess());
    }

    @Test
    public void testCreateNonExistingSubFolder() {
        String path = "/testFolder/1/2/3/4/5/";
        String top = "/testFolder/";

        assertTrue(new CreateFolderRemoteOperation(path, true).execute(client).isSuccess());

        // verify folder
        assertTrue(new ReadFolderRemoteOperation(path).execute(client).isSuccess());

        // remove folder
        assertTrue(new RemoveFileRemoteOperation(top).execute(client).isSuccess());
    }

    @Test
    public void testCreateFolderWithWrongURL() {
        String path = "/testFolder/";
        Uri uri = client.getBaseUri();
        client.setBaseUri(Uri.parse(uri.toString() + "/remote.php/webdav"));

        // create folder
        assertFalse(new CreateFolderRemoteOperation(path, true).execute(client).isSuccess());

        client.setBaseUri(uri);
    }
}
