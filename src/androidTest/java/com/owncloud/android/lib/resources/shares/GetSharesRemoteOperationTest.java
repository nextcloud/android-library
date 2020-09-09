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

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

public class GetSharesRemoteOperationTest extends AbstractIT {
    private static final String ENTITY_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String ENTITY_CHARSET = "UTF-8";

    @Test
    public void searchSharedFiles() throws Exception {
        assertTrue(new CreateFolderRemoteOperation("/shareToAdmin/", true).execute(client).isSuccess());
        assertTrue(new CreateFolderRemoteOperation("/shareToGroup/", true).execute(client).isSuccess());
        assertTrue(new CreateFolderRemoteOperation("/shareViaLink/", true).execute(client).isSuccess());
//        assertTrue(new CreateFolderRemoteOperation("/shareViaMail/", true).execute(client).isSuccess());
        assertTrue(new CreateFolderRemoteOperation("/noShare/", true).execute(client).isSuccess());
        //assertTrue(new CreateFolderRemoteOperation("/shareToCircle/", true).execute(client).isSuccess());

        GetSharesRemoteOperation sut = new GetSharesRemoteOperation();

        RemoteOperationResult result = sut.execute(client);
        assertTrue(result.isSuccess());

        assertEquals(0, result.getData().size());

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
//        Assert.assertTrue(circleResult.getLogMessage(), circleResult.isSuccess());

        // share folder to mail
//        Assert.assertTrue(new CreateShareRemoteOperation("/shareViaMail/",
//                ShareType.EMAIL,
//                "testUser@testcloudserver.com",
//                false,
//                "",
//                OCShare.DEFAULT_PERMISSION)
//                .execute(client).isSuccess());

        sut = new GetSharesRemoteOperation();

        result = sut.execute(client);
        assertTrue(result.isSuccess());

        assertEquals(3, result.getData().size());

        for (Object object : result.getData()) {
            OCShare ocShare = (OCShare) object;

            switch (ocShare.getShareType()) {
                case USER:
                    assertEquals("/shareToAdmin/", ocShare.getPath());
                    break;

                case PUBLIC_LINK:
                    assertEquals("/shareViaLink/", ocShare.getPath());
                    break;

                case GROUP:
                    assertEquals("/shareToGroup/", ocShare.getPath());
                    break;

                case CIRCLE:
                    assertEquals("/shareToCircle/", ocShare.getPath());
                    break;

//                case EMAIL:
//                    assertEquals("/shareViaMail/", ocShare.getPath());
//                    break;

                default:
                    throw new AssertionError("Unknown share type");
            }
        }
    }
}
