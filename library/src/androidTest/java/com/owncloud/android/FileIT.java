/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2018-2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import com.owncloud.android.lib.resources.status.GetCapabilitiesRemoteOperation;
import com.owncloud.android.lib.resources.status.NextcloudVersion;
import com.owncloud.android.lib.resources.status.OCCapability;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tests related to file operations
 */
public class FileIT extends AbstractIT {
    @Test
    public void testCreateFolderSuccess() {
        String path = "/testFolder/";

        // create folder
        assertTrue(new CreateFolderRemoteOperation(path, true).execute(nextcloudClient).isSuccess());

        // verify folder
        assertTrue(new ReadFolderRemoteOperation(path).execute(nextcloudClient).isSuccess());
        assertTrue(new ReadFolderRemoteOperation(path).execute(nextcloudClient).isSuccess());

        // remove folder
        assertTrue(new RemoveFileRemoteOperation(path).execute(nextcloudClient).isSuccess());
    }

    @Test
    public void testCreateFolderFailure() {
        String path = "/testFolder/";

        // create folder
        assertTrue(new CreateFolderRemoteOperation(path, true).execute(nextcloudClient).isSuccess());

        // create folder a second time will fail
        assertFalse(new CreateFolderRemoteOperation(path, true).execute(nextcloudClient).isSuccess());

        // remove folder
        assertTrue(new RemoveFileRemoteOperation(path).execute(nextcloudClient).isSuccess());
    }

    @Test
    public void testCreateNonExistingSubFolder() {
        String path = "/testFolder/1/2/3/4/5/";
        String top = "/testFolder/";

        assertTrue(new CreateFolderRemoteOperation(path, true).execute(nextcloudClient).isSuccess());

        // verify folder
        assertTrue(new ReadFolderRemoteOperation(path).execute(nextcloudClient).isSuccess());

        // remove folder
        assertTrue(new RemoveFileRemoteOperation(top).execute(nextcloudClient).isSuccess());
    }

    @Test
    public void testCreateFolderWithWrongURL() {
        String path = "/testFolder/";
        Uri uri = nextcloudClient.getBaseUri();
        assertNotNull(uri);
        nextcloudClient.setBaseUri(Uri.parse(uri + "/remote.php/dav/files/"));

        // create folder
        assertFalse(new CreateFolderRemoteOperation(path, true).execute(nextcloudClient).isSuccess());

        nextcloudClient.setBaseUri(uri);
    }

    @Test
    public void testZeroSharees() {
        // create & verify folder
        String path = "/testFolder/";
        assertTrue(new CreateFolderRemoteOperation(path, true).execute(nextcloudClient).isSuccess());
        assertTrue(new ReadFolderRemoteOperation(path).execute(nextcloudClient).isSuccess());

        // verify
        RemoteOperationResult<List<RemoteFile>> result = new ReadFolderRemoteOperation("/").execute(nextcloudClient);
        assertTrue(result.isSuccess());

        List<RemoteFile> resultData = result.getResultData();
        assertNotNull(resultData);
        assertEquals("/", resultData.get(0).getRemotePath());

        for (RemoteFile remoteFile : resultData) {
            if (path.equals(remoteFile.getRemotePath())) {
                assertNotNull(remoteFile.getSharees());
                assertEquals(0, remoteFile.getSharees().length);
                break;
            }
        }
    }

    @Test
    public void testShareViaLinkSharees() {
        // create & verify folder
        String path = "/testFolder/";
        assertTrue(new CreateFolderRemoteOperation(path, true).execute(nextcloudClient).isSuccess());
        assertTrue(new ReadFolderRemoteOperation(path).execute(nextcloudClient).isSuccess());

        // share folder
        assertTrue(new CreateShareRemoteOperation(path,
                                                  ShareType.PUBLIC_LINK,
                                                  "",
                                                  false,
                                                  "",
                                                  OCShare.NO_PERMISSION)
                           .execute(client).isSuccess());

        // verify
        RemoteOperationResult<List<RemoteFile>> result = new ReadFolderRemoteOperation(path).execute(nextcloudClient);
        assertTrue(result.isSuccess());

        List<RemoteFile> resultData = result.getResultData();
        assertNotNull(resultData);
        assertEquals(path, resultData.get(0).getRemotePath());

        ShareeUser[] sharees = resultData.get(0).getSharees();
        assertNotNull(sharees);
        assertEquals(0, sharees.length);
    }

    @Test
    public void testShareToGroupSharees() {
        // create & verify folder
        String path = "/testFolder/";
        assertTrue(new CreateFolderRemoteOperation(path, true).execute(nextcloudClient).isSuccess());
        assertTrue(new ReadFolderRemoteOperation(path).execute(nextcloudClient).isSuccess());

        ShareeUser sharee = new ShareeUser("users", "", ShareType.GROUP);

        // only on NC26+
        OCCapability ocCapability = new GetCapabilitiesRemoteOperation().execute(nextcloudClient).getResultData();

        assertNotNull(ocCapability);

        if (ocCapability.getVersion().isNewerOrEqual(NextcloudVersion.nextcloud_26)) {
            sharee.setDisplayName("users");
        }

        // share folder
        assertTrue(new CreateShareRemoteOperation(path,
                ShareType.GROUP,
                "users",
                false,
                "",
                OCShare.NO_PERMISSION)
                .execute(client).isSuccess());

        // verify
        RemoteOperationResult<List<RemoteFile>> result = new ReadFolderRemoteOperation("/").execute(nextcloudClient);
        assertTrue(result.isSuccess());

        List<RemoteFile> resultData = result.getResultData();
        assertNotNull(resultData);
        assertEquals("/", resultData.get(0).getRemotePath());

        for (RemoteFile remoteFile : resultData) {
            if (path.equals(remoteFile.getRemotePath())) {
                assertNotNull(remoteFile.getSharees());
                assertEquals(1, remoteFile.getSharees().length);
                assertEquals(sharee, remoteFile.getSharees()[0]);
            }
        }
    }

    @Test
    public void testOneSharees() {
        // create & verify folder
        String path = "/testFolder/";
        assertTrue(new CreateFolderRemoteOperation(path, true).execute(nextcloudClient).isSuccess());
        assertTrue(new ReadFolderRemoteOperation(path).execute(nextcloudClient).isSuccess());

        ShareeUser sharee = new ShareeUser("user1", "User One", ShareType.USER);

        // share folder
        assertTrue(new CreateShareRemoteOperation(path,
                                                  ShareType.USER,
                                                  "user1",
                                                  false,
                                                  "",
                                                  OCShare.NO_PERMISSION)
                           .execute(client).isSuccess());

        // verify
        RemoteOperationResult<List<RemoteFile>> result = new ReadFolderRemoteOperation("/").execute(nextcloudClient);
        assertTrue(result.isSuccess());

        List<RemoteFile> resultData = result.getResultData();
        assertNotNull(resultData);
        assertEquals("/", resultData.get(0).getRemotePath());

        for (RemoteFile remoteFile : resultData) {
            if (path.equals(remoteFile.getRemotePath())) {
                assertNotNull(remoteFile.getSharees());
                assertEquals(1, remoteFile.getSharees().length);
                assertEquals(sharee, remoteFile.getSharees()[0]);
            }
        }
    }

    @Test
    public void testTwoShareesOnParent() {
        // create & verify folder
        String path = "/testFolder/";
        assertTrue(new CreateFolderRemoteOperation(path, true).execute(nextcloudClient).isSuccess());
        assertTrue(new ReadFolderRemoteOperation(path).execute(nextcloudClient).isSuccess());

        List<ShareeUser> sharees = new ArrayList<>();
        sharees.add(new ShareeUser("user1", "User One", ShareType.USER));
        sharees.add(new ShareeUser("user2", "User Two", ShareType.USER));

        // share folder
        assertTrue(new CreateShareRemoteOperation(path,
                                                  ShareType.USER,
                                                  "user1",
                                                  false,
                                                  "",
                                                  OCShare.NO_PERMISSION)
                           .execute(client).isSuccess());

        assertTrue(new CreateShareRemoteOperation(path,
                                                  ShareType.USER,
                                                  "user2",
                                                  false,
                                                  "",
                                                  OCShare.NO_PERMISSION)
                           .execute(client).isSuccess());

        // verify
        RemoteOperationResult<List<RemoteFile>> result = new ReadFolderRemoteOperation("/").execute(nextcloudClient);
        assertTrue(result.isSuccess());

        List<RemoteFile> resultData = result.getResultData();
        assertNotNull(resultData);
        assertEquals("/", resultData.get(0).getRemotePath());

        for (RemoteFile remoteFile : resultData) {
            if (path.equals(remoteFile.getRemotePath())) {
                assertNotNull(remoteFile.getSharees());
                assertEquals(2, remoteFile.getSharees().length);

                assertTrue(sharees.containsAll(Arrays.asList(remoteFile.getSharees())));
            }
        }
    }

    @Test
    public void testTwoSharees() {
        // create & verify folder
        String path = "/testFolder/";
        assertTrue(new CreateFolderRemoteOperation(path, true).execute(nextcloudClient).isSuccess());
        assertTrue(new ReadFolderRemoteOperation(path).execute(nextcloudClient).isSuccess());

        List<ShareeUser> sharees = new ArrayList<>();
        sharees.add(new ShareeUser("user1", "User One", ShareType.USER));
        sharees.add(new ShareeUser("user2", "User Two", ShareType.USER));

        // share folder
        assertTrue(new CreateShareRemoteOperation(path,
                                                  ShareType.USER,
                                                  "user1",
                                                  false,
                                                  "",
                                                  OCShare.NO_PERMISSION)
                           .execute(client).isSuccess());

        assertTrue(new CreateShareRemoteOperation(path,
                                                  ShareType.USER,
                                                  "user2",
                                                  false,
                                                  "",
                                                  OCShare.NO_PERMISSION)
                           .execute(client).isSuccess());

        // verify
        RemoteOperationResult<List<RemoteFile>> result = new ReadFolderRemoteOperation(path).execute(nextcloudClient);
        assertTrue(result.isSuccess());
        assertNotNull(result.getResultData());

        RemoteFile folder = result.getResultData().get(0);
        assertNotNull(folder);
        assertEquals(path, folder.getRemotePath());
        assertNotNull(folder.getSharees());
        assertEquals(2, folder.getSharees().length);

        for (ShareeUser user : folder.getSharees()) {
            assertTrue(sharees.contains(user));
        }
    }

    @Test
    public void testLocalID() {
        // create & verify folder
        String path = "/testFolder/";
        assertTrue(new CreateFolderRemoteOperation(path, true).execute(nextcloudClient).isSuccess());

        RemoteOperationResult<List<RemoteFile>> result = new ReadFolderRemoteOperation(path).execute(nextcloudClient);
        assertTrue(result.isSuccess());
        assertNotNull(result.getResultData());

        RemoteFile folder = result.getResultData().get(0);

        // we do this only here for testing, this might not work on large installations
        assertNotNull(folder.getRemoteId());
        int localId = Integer.parseInt(folder.getRemoteId().substring(0, 8).replaceAll("^0*", ""));

        assertEquals(folder.getLocalId(), localId);
    }
}
