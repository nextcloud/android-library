/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019-2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.shares;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import android.net.Uri;
import android.os.Bundle;

import androidx.test.platform.app.InstrumentationRegistry;

import com.nextcloud.common.NextcloudClient;
import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.OwnCloudBasicCredentials;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.ToggleFavoriteRemoteOperation;
import com.owncloud.android.lib.resources.status.GetCapabilitiesRemoteOperation;
import com.owncloud.android.lib.resources.status.NextcloudVersion;
import com.owncloud.android.lib.resources.status.OCCapability;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.List;

import okhttp3.Credentials;

public class GetSharesRemoteOperationIT extends AbstractIT {
    @Test
    public void searchSharedFiles() {
        assertTrue(new CreateFolderRemoteOperation("/shareToAdmin/", true).execute(nextcloudClient).isSuccess());
        assertTrue(new CreateFolderRemoteOperation("/shareToGroup/", true).execute(nextcloudClient).isSuccess());
        assertTrue(new CreateFolderRemoteOperation("/shareViaLink/", true).execute(nextcloudClient).isSuccess());
        assertTrue(new CreateFolderRemoteOperation("/noShare/", true).execute(nextcloudClient).isSuccess());

        GetSharesRemoteOperation sut = new GetSharesRemoteOperation();

        RemoteOperationResult<List<OCShare>> result = sut.execute(client);
        assertTrue(result.isSuccess());

        assertEquals(0, result.getResultData().size());

        // share folder to user "admin"
        assertTrue(new CreateShareRemoteOperation("/shareToAdmin/",
                ShareType.USER,
                "admin",
                false,
                "",
                OCShare.MAXIMUM_PERMISSIONS_FOR_FOLDER)
                .execute(client).isSuccess());

        // share folder via public link
        assertTrue(new CreateShareRemoteOperation("/shareViaLink/",
                ShareType.PUBLIC_LINK,
                "",
                true,
                "",
                OCShare.READ_PERMISSION_FLAG)
                .execute(client).isSuccess());

        // share folder to group
        assertTrue(new CreateShareRemoteOperation("/shareToGroup/",
                ShareType.GROUP,
                "users",
                false,
                "",
                OCShare.NO_PERMISSION)
                .execute(client).isSuccess());

        // share folder to circle
        // get share 
//        RemoteOperationResult searchResult = new GetShareesRemoteOperation("publicCircle", 1, 50).execute(client);
//        assertTrue(searchResult.getLogMessage(), searchResult.isSuccess());
//
//        JSONObject resultJson = (JSONObject) searchResult.getData().get(0);
//        String circleId = resultJson.getJSONObject("value").getString("shareWith");
//
//        RemoteOperationResult circleResult = new CreateShareRemoteOperation("/shareToCircle/",
//                ShareType.CIRCLE,
//                circleId,
//                false,
//                "",
//                OCShare.DEFAULT_PERMISSION)
//                .execute(client);
//        assertTrue(circleResult.getLogMessage(), circleResult.isSuccess());

        // share folder to mail
//        assertTrue(new CreateShareRemoteOperation("/shareViaMail/",
//                ShareType.EMAIL,
//                "testUser@testcloudserver.com",
//                false,
//                "",
//                OCShare.DEFAULT_PERMISSION)
//                .execute(client).isSuccess());

        sut = new GetSharesRemoteOperation();

        result = sut.execute(client);
        assertTrue(result.isSuccess());

        assertEquals(3, result.getResultData().size());

        final Bundle arguments = InstrumentationRegistry.getArguments();
        final String loginName = arguments.getString("TEST_SERVER_USERNAME");

        for (OCShare ocShare : result.getResultData()) {
            switch (ocShare.getShareType()) {
                case USER:
                    assertEquals("/shareToAdmin/", ocShare.getPath());
                    assertFolderAttributes(ocShare, loginName);
                    break;

                case PUBLIC_LINK:
                    assertEquals("/shareViaLink/", ocShare.getPath());
                    assertFolderAttributes(ocShare, loginName);
                    break;

                case GROUP:
                    assertEquals("/shareToGroup/", ocShare.getPath());
                    assertFolderAttributes(ocShare, loginName);
                    break;

                case CIRCLE:
                    assertEquals("/shareToCircle/", ocShare.getPath());
                    assertFolderAttributes(ocShare, loginName);
                    break;

//                case EMAIL:
//                    assertEquals("/shareViaMail/", ocShare.getPath());
//                    break;

                default:
                    throw new AssertionError("Unknown share type");
            }
        }
    }

    private void assertFolderAttributes(final OCShare share, final String expectedDisplayName) {
        assertEquals(expectedDisplayName, share.getOwnerDisplayName());
        assertEquals("httpd/unix-directory", share.getMimetype());
        assertFalse(share.isHasPreview());
    }

    @Test
    public void sharedWithMe() {
        GetSharesRemoteOperation sut = new GetSharesRemoteOperation();
        GetSharesRemoteOperation sutSharedWithMe = new GetSharesRemoteOperation(true);

        RemoteOperationResult<List<OCShare>> result = sut.execute(client);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getResultData().size());

        RemoteOperationResult<List<OCShare>> resultSharedWithMe = sutSharedWithMe.execute(client);
        assertTrue(resultSharedWithMe.isSuccess());
        assertEquals(0, resultSharedWithMe.getResultData().size());


        // share folder to user "admin"
        assertTrue(new CreateFolderRemoteOperation("/shareToAdmin/", true).execute(nextcloudClient).isSuccess());
        assertTrue(new CreateShareRemoteOperation("/shareToAdmin/",
                ShareType.USER,
                "admin",
                false,
                "",
                OCShare.MAXIMUM_PERMISSIONS_FOR_FOLDER)
                .execute(client).isSuccess());

        // Expect one file shared by me, no file shared with me
        result = sut.execute(client);
        assertEquals(1, result.getResultData().size());

        resultSharedWithMe = sutSharedWithMe.execute(client);
        assertEquals(0, resultSharedWithMe.getResultData().size());

        // create client for user "user1"
        Bundle arguments = InstrumentationRegistry.getArguments();
        url = Uri.parse(arguments.getString("TEST_SERVER_URL"));
        String loginName = "user1";
        String password = "user1";

        OwnCloudClient clientUser1 = OwnCloudClientFactory.createOwnCloudClient(url, context, true);
        clientUser1.setCredentials(new OwnCloudBasicCredentials(loginName, password));
        clientUser1.setUserId(loginName); // for test same as userId

        String credentials = Credentials.basic(loginName, password);
        NextcloudClient nextcloudClientUser1 = new NextcloudClient(url, loginName, credentials, context);

        // share folder to previous user
        assertTrue(new CreateFolderRemoteOperation("/shareToUser/", true).execute(nextcloudClientUser1).isSuccess());
        assertTrue(new CreateShareRemoteOperation("/shareToUser/",
                ShareType.USER,
                client.getCredentials().getUsername(),
                false,
                "",
                OCShare.MAXIMUM_PERMISSIONS_FOR_FOLDER)
                .execute(clientUser1).isSuccess());

        // Expect one file shared by me, one file shared with me
        result = sut.execute(client);
        assertEquals(1, result.getResultData().size());

        resultSharedWithMe = sutSharedWithMe.execute(client);
        assertEquals(1, resultSharedWithMe.getResultData().size());
    }

    @Test
    public void favorites() {
        // only on NC25+
        testOnlyOnServer(NextcloudVersion.nextcloud_25);

        // share folder to user "admin"
        assertTrue(new CreateFolderRemoteOperation("/shareToAdminNoFavorite/", true).execute(nextcloudClient).isSuccess());
        RemoteOperationResult<List<OCShare>> createResult = new CreateShareRemoteOperation("/shareToAdminNoFavorite/",
                ShareType.USER,
                "admin",
                false,
                "",
                OCShare.MAXIMUM_PERMISSIONS_FOR_FOLDER,
                true)
                .execute(client);

        assertTrue(createResult.isSuccess());

        String path = "/shareToAdminFavorite/";
        assertTrue(new CreateFolderRemoteOperation(path, true).execute(nextcloudClient).isSuccess());

        // favorite it
        TestCase.assertTrue(new ToggleFavoriteRemoteOperation(true, path).execute(client).isSuccess());

        // share folder to user "admin"
        createResult = new CreateShareRemoteOperation(path,
                ShareType.USER,
                "admin",
                false,
                "",
                OCShare.MAXIMUM_PERMISSIONS_FOR_FOLDER,
                true)
                .execute(client);

        assertTrue(createResult.isSuccess());

        // check
        RemoteOperationResult<List<OCShare>> sut = new GetSharesRemoteOperation(false).execute(client);
        assertEquals(2, sut.getResultData().size());
        assertFalse(sut.getResultData().get(0).isFavorite());
        assertTrue(sut.getResultData().get(1).isFavorite());
    }

    @Test
    public void noFavorite() {
        // only on NC25+
        testOnlyOnServer(NextcloudVersion.nextcloud_25);

        assertTrue(new CreateFolderRemoteOperation("/shareToAdminNoFavorite/", true).execute(nextcloudClient).isSuccess());

        // share folder to user "admin"
        RemoteOperationResult<List<OCShare>> createResult = new CreateShareRemoteOperation("/shareToAdminNoFavorite/",
                ShareType.USER,
                "admin",
                false,
                "",
                OCShare.MAXIMUM_PERMISSIONS_FOR_FOLDER,
                true)
                .execute(client);

        assertTrue(createResult.isSuccess());

        OCShare share = createResult.getResultData().get(0);

        assertFalse(share.isFavorite());
    }

    @Test
    public void favorite() {
        // only on NC25+
        OCCapability ocCapability = new GetCapabilitiesRemoteOperation().execute(nextcloudClient).getResultData();
        assumeTrue(ocCapability != null);
        assumeTrue(ocCapability.getVersion().isNewerOrEqual(NextcloudVersion.nextcloud_25));

        String path = "/shareToAdminFavorite/";
        assertTrue(new CreateFolderRemoteOperation(path, true).execute(nextcloudClient).isSuccess());

        // favorite it
        TestCase.assertTrue(new ToggleFavoriteRemoteOperation(true, path).execute(client).isSuccess());

        // share folder to user "admin"
        RemoteOperationResult<List<OCShare>> createResult = new CreateShareRemoteOperation(path,
                ShareType.USER,
                "admin",
                false,
                "",
                OCShare.MAXIMUM_PERMISSIONS_FOR_FOLDER,
                true)
                .execute(client);

        assertTrue(createResult.isSuccess());

        OCShare share = createResult.getResultData().get(0);

        assertTrue(share.isFavorite());
    }
}
