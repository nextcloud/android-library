package com.owncloud.android;

import android.net.Uri;

import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.RemoveFileRemoteOperation;
import com.owncloud.android.lib.resources.files.model.RemoteFile;
import com.owncloud.android.lib.resources.shares.CreateShareRemoteOperation;
import com.owncloud.android.lib.resources.shares.OCShare;
import com.owncloud.android.lib.resources.shares.ShareType;
import com.owncloud.android.lib.resources.shares.ShareeUser;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests related to file operations
 */
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

    @Test
    public void testZeroSharees() {
        // create & verify folder
        String path = "/testFolder/";
        assertTrue(new CreateFolderRemoteOperation(path, true).execute(client).isSuccess());
        assertTrue(new ReadFolderRemoteOperation(path).execute(client).isSuccess());

        // verify
        RemoteOperationResult result = new ReadFolderRemoteOperation("/").execute(client);
        assertTrue(result.isSuccess());

        RemoteFile parentFolder = (RemoteFile) result.getData().get(0);
        assertEquals("/", parentFolder.getRemotePath());

        for (int i = 1; i < result.getData().size(); i++) {
            RemoteFile child = (RemoteFile) result.getData().get(i);

            if (path.equals(child.getRemotePath())) {
                assertEquals(0, child.getSharees().length);
            }
        }
    }

    @Test
    public void testShareViaLinkSharees() {
        // create & verify folder
        String path = "/testFolder/";
        assertTrue(new CreateFolderRemoteOperation(path, true).execute(client).isSuccess());
        assertTrue(new ReadFolderRemoteOperation(path).execute(client).isSuccess());

        // share folder
        assertTrue(new CreateShareRemoteOperation(path,
                                                  ShareType.PUBLIC_LINK,
                                                  "",
                                                  false,
                                                  "",
                                                  OCShare.DEFAULT_PERMISSION)
                           .execute(client).isSuccess());

        // verify
        RemoteOperationResult result = new ReadFolderRemoteOperation("/").execute(client);
        assertTrue(result.isSuccess());

        RemoteFile parentFolder = (RemoteFile) result.getData().get(0);
        assertEquals("/", parentFolder.getRemotePath());

        for (int i = 1; i < result.getData().size(); i++) {
            RemoteFile child = (RemoteFile) result.getData().get(i);

            if (path.equals(child.getRemotePath())) {
                assertEquals(0, child.getSharees().length);
            }
        }
    }

    @Test
    public void testShareToGroupSharees() {
        // create & verify folder
        String path = "/testFolder/";
        assertTrue(new CreateFolderRemoteOperation(path, true).execute(client).isSuccess());
        assertTrue(new ReadFolderRemoteOperation(path).execute(client).isSuccess());

        ShareeUser sharee = new ShareeUser("users", "", ShareType.GROUP);

        // share folder
        assertTrue(new CreateShareRemoteOperation(path,
                                                  ShareType.GROUP,
                                                  "users",
                                                  false,
                                                  "",
                                                  OCShare.DEFAULT_PERMISSION)
                           .execute(client).isSuccess());

        // verify
        RemoteOperationResult result = new ReadFolderRemoteOperation("/").execute(client);
        assertTrue(result.isSuccess());

        RemoteFile parentFolder = (RemoteFile) result.getData().get(0);
        assertEquals("/", parentFolder.getRemotePath());

        for (int i = 1; i < result.getData().size(); i++) {
            RemoteFile child = (RemoteFile) result.getData().get(i);

            if (path.equals(child.getRemotePath())) {
                assertEquals(1, child.getSharees().length);
                assertEquals(sharee, child.getSharees()[0]);
            }
        }
    }

    @Test
    public void testOneSharees() {
        // create & verify folder
        String path = "/testFolder/";
        assertTrue(new CreateFolderRemoteOperation(path, true).execute(client).isSuccess());
        assertTrue(new ReadFolderRemoteOperation(path).execute(client).isSuccess());

        ShareeUser sharee = new ShareeUser("user1", "User One", ShareType.USER);

        // share folder
        assertTrue(new CreateShareRemoteOperation(path,
                                                  ShareType.USER,
                                                  "user1",
                                                  false,
                                                  "",
                                                  OCShare.DEFAULT_PERMISSION)
                           .execute(client).isSuccess());

        // verify
        RemoteOperationResult result = new ReadFolderRemoteOperation("/").execute(client);
        assertTrue(result.isSuccess());

        RemoteFile parentFolder = (RemoteFile) result.getData().get(0);
        assertEquals("/", parentFolder.getRemotePath());

        for (int i = 1; i < result.getData().size(); i++) {
            RemoteFile child = (RemoteFile) result.getData().get(i);

            if (path.equals(child.getRemotePath())) {
                assertEquals(1, child.getSharees().length);
                assertEquals(sharee, child.getSharees()[0]);
            }
        }
    }

    @Test
    public void testTwoShareesOnParent() {
        // create & verify folder
        String path = "/testFolder/";
        assertTrue(new CreateFolderRemoteOperation(path, true).execute(client).isSuccess());
        assertTrue(new ReadFolderRemoteOperation(path).execute(client).isSuccess());

        List<ShareeUser> sharees = new ArrayList<>();
        sharees.add(new ShareeUser("user1", "User One", ShareType.USER));
        sharees.add(new ShareeUser("user2", "User Two", ShareType.USER));

        // share folder
        assertTrue(new CreateShareRemoteOperation(path,
                                                  ShareType.USER,
                                                  "user1",
                                                  false,
                                                  "",
                                                  OCShare.DEFAULT_PERMISSION)
                           .execute(client).isSuccess());

        assertTrue(new CreateShareRemoteOperation(path,
                                                  ShareType.USER,
                                                  "user2",
                                                  false,
                                                  "",
                                                  OCShare.DEFAULT_PERMISSION)
                           .execute(client).isSuccess());

        // verify
        RemoteOperationResult result = new ReadFolderRemoteOperation("/").execute(client);
        assertTrue(result.isSuccess());

        RemoteFile parentFolder = (RemoteFile) result.getData().get(0);
        assertEquals("/", parentFolder.getRemotePath());

        for (int i = 1; i < result.getData().size(); i++) {
            RemoteFile child = (RemoteFile) result.getData().get(i);

            if (path.equals(child.getRemotePath())) {
                assertEquals(2, child.getSharees().length);

                for (ShareeUser user : child.getSharees()) {
                    assertTrue(sharees.contains(user));
                }
            }
        }
    }

    @Test
    public void testTwoSharees() {
        // create & verify folder
        String path = "/testFolder/";
        assertTrue(new CreateFolderRemoteOperation(path, true).execute(client).isSuccess());
        assertTrue(new ReadFolderRemoteOperation(path).execute(client).isSuccess());

        List<ShareeUser> sharees = new ArrayList<>();
        sharees.add(new ShareeUser("user1", "User One", ShareType.USER));
        sharees.add(new ShareeUser("user2", "User Two", ShareType.USER));

        // share folder
        assertTrue(new CreateShareRemoteOperation(path,
                                                  ShareType.USER,
                                                  "user1",
                                                  false,
                                                  "",
                                                  OCShare.DEFAULT_PERMISSION)
                           .execute(client).isSuccess());

        assertTrue(new CreateShareRemoteOperation(path,
                                                  ShareType.USER,
                                                  "user2",
                                                  false,
                                                  "",
                                                  OCShare.DEFAULT_PERMISSION)
                           .execute(client).isSuccess());

        // verify
        RemoteOperationResult result = new ReadFolderRemoteOperation(path).execute(client);
        assertTrue(result.isSuccess());

        RemoteFile folder = (RemoteFile) result.getData().get(0);
        assertEquals(path, folder.getRemotePath());
        assertEquals(2, folder.getSharees().length);

        for (ShareeUser user : folder.getSharees()) {
            assertTrue(sharees.contains(user));
        }
    }
}
