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

package com.owncloud.android.lib.resources.shares;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import android.net.Uri;
import android.os.Bundle;

import androidx.test.platform.app.InstrumentationRegistry;

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

public class GetSharesRemoteOperationIT extends AbstractIT {
    @Test
    public void searchSharedFiles() {
        assertTrue(new CreateFolderRemoteOperation("/shareToAdmin/", true).execute(client).isSuccess());
        assertTrue(new CreateFolderRemoteOperation("/shareToGroup/", true).execute(client).isSuccess());
        assertTrue(new CreateFolderRemoteOperation("/shareViaLink/", true).execute(client).isSuccess());
//        assertTrue(new CreateFolderRemoteOperation("/shareViaMail/", true).execute(client).isSuccess());
        assertTrue(new CreateFolderRemoteOperation("/noShare/", true).execute(client).isSuccess());
        //assertTrue(new CreateFolderRemoteOperation("/shareToCircle/", true).execute(client).isSuccess());

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
        assertTrue(new CreateFolderRemoteOperation("/shareToAdmin/", true).execute(client).isSuccess());
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

        // share folder to previous user
        assertTrue(new CreateFolderRemoteOperation("/shareToUser/", true).execute(clientUser1).isSuccess());
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
        OCCapability ocCapability = (OCCapability) new GetCapabilitiesRemoteOperation()
                .execute(nextcloudClient).getSingleData();
        assumeTrue(ocCapability.getVersion().isNewerOrEqual(NextcloudVersion.nextcloud_25));

        // share folder to user "admin"
        assertTrue(new CreateFolderRemoteOperation("/shareToAdminNoFavorite/", true).execute(client).isSuccess());
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
        assertTrue(new CreateFolderRemoteOperation(path, true).execute(client).isSuccess());

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
        OCCapability ocCapability = (OCCapability) new GetCapabilitiesRemoteOperation()
                .execute(nextcloudClient).getSingleData();
        assumeTrue(ocCapability.getVersion().isNewerOrEqual(NextcloudVersion.nextcloud_25));

        assertTrue(new CreateFolderRemoteOperation("/shareToAdminNoFavorite/", true).execute(client).isSuccess());

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
        OCCapability ocCapability = (OCCapability) new GetCapabilitiesRemoteOperation()
                .execute(nextcloudClient).getSingleData();
        assumeTrue(ocCapability.getVersion().isNewerOrEqual(NextcloudVersion.nextcloud_25));

        String path = "/shareToAdminFavorite/";
        assertTrue(new CreateFolderRemoteOperation(path, true).execute(client).isSuccess());

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
