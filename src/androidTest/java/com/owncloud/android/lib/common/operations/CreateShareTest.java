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
package com.owncloud.android.lib.common.operations;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode;
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation;
import com.owncloud.android.lib.resources.shares.CreateShareRemoteOperation;
import com.owncloud.android.lib.resources.shares.ShareType;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test create share
 */
public class CreateShareTest extends AbstractIT {
    private static final String NON_EXISTENT_FILE = "/nonExistentFile.txt";
    private static final String serverUri2 = "server2";

    private String mFullPath2FileToShare;
    private String mFullPath2NonExistentFile;

    @Before
    public void setUp() throws Exception {
        mFullPath2FileToShare = ASSETS__TEXT_FILE_NAME;
        mFullPath2NonExistentFile = NON_EXISTENT_FILE;

        File textFile = getFile(ASSETS__TEXT_FILE_NAME);
        RemoteOperationResult result = new UploadFileRemoteOperation(textFile.getAbsolutePath(),
                                                                     mFullPath2FileToShare,
                                                                     "txt/plain",
                                                                     String.valueOf(System.currentTimeMillis() / 1000))
                .execute(client);

        assertTrue("Error uploading file " + textFile + ": " + result, result.isSuccess());
    }

    @Test
    public void testCreatePublicShareSuccessful() {
        RemoteOperationResult result = new CreateShareRemoteOperation(mFullPath2FileToShare,
                                                                      ShareType.PUBLIC_LINK,
                                                                      "",
                                                                      false,
                                                                      "",
                                                                      1).execute(client);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testCreatePublicShareFailure() {
        RemoteOperationResult result = new CreateShareRemoteOperation(mFullPath2NonExistentFile,
                                                                      ShareType.PUBLIC_LINK,
                                                                      "",
                                                                      false,
                                                                      "",
                                                                      1).execute(client);

        assertFalse(result.isSuccess());
        assertEquals(ResultCode.FILE_NOT_FOUND, result.getCode());
    }

    /**
     * Test creation of private shares with groups
     */
    @Test
    public void testCreatePrivateShareWithUserSuccessful() {
        RemoteOperationResult result = new CreateShareRemoteOperation(mFullPath2FileToShare,
                                                                      ShareType.USER,
                                                                      "admin",
                                                                      false,
                                                                      "",
                                                                      31).execute(client);
        assertTrue(result.isSuccess());
    }

    /**
     * Test creation of private shares with groups
     */
    @Test
    public void testCreatePrivateShareWithUserNotExists() {
        RemoteOperationResult result = new CreateShareRemoteOperation(mFullPath2FileToShare,
                                                                      ShareType.USER,
                                                                      "no_exist",
                                                                      false,
                                                                      "",
                                                                      31).execute(client);
        assertFalse(result.isSuccess());

        // TODO 404 is File not found, but actually it is "user not found"
        assertEquals(ResultCode.FILE_NOT_FOUND, result.getCode());
    }

    /**
     * Test creation of private shares with groups
     */
    @Test
    public void testCreatePrivateShareWithFileNotExists() {
        RemoteOperationResult result = new CreateShareRemoteOperation(mFullPath2NonExistentFile,
                                                                      ShareType.USER,
                                                                      "admin",
                                                                      false,
                                                                      "",
                                                                      31).execute(client);
        assertFalse(result.isSuccess());
        assertEquals(ResultCode.FILE_NOT_FOUND, result.getCode());
    }

    /**
     * Test creation of private shares with groups
     */
    @Test
    public void testCreatePrivateShareWithGroupSuccessful() {
        RemoteOperationResult result = new CreateShareRemoteOperation(mFullPath2FileToShare,
                                                                      ShareType.GROUP,
                                                                      "admin",
                                                                      false,
                                                                      "",
                                                                      1).execute(client);
        assertTrue(result.isSuccess());
    }

    /**
     * Test creation of private shares with groups
     */
    @Test
    public void testCreatePrivateShareWithNonExistingGroupSharee() {
        RemoteOperationResult result = new CreateShareRemoteOperation(mFullPath2FileToShare,
                                                                      ShareType.GROUP,
                                                                      "no_exist",
                                                                      false,
                                                                      "",
                                                                      31).execute(client);
        assertFalse(result.isSuccess());

        // TODO 404 is File not found, but actually it is "user not found"
        assertEquals(ResultCode.FILE_NOT_FOUND, result.getCode());
    }

    /**
     * Test creation of private shares with groups
     */
    @Test
    public void testCreatePrivateShareWithNonExistingFile() {
        RemoteOperationResult result = new CreateShareRemoteOperation(mFullPath2NonExistentFile,
                                                                      ShareType.GROUP,
                                                                      "admin",
                                                                      false,
                                                                      "",
                                                                      31).execute(client);
        assertFalse(result.isSuccess());
        assertEquals(ResultCode.FILE_NOT_FOUND, result.getCode());
    }

//    /**
//     * Test creation of federated shares with remote users
//     */
//    @Test
//    public void testCreateFederatedShareWithUser() {
//        RemoteOperationResult result = new CreateRemoteShareOperation(mFullPath2FileToShare,
//                                                                      ShareType.FEDERATED,
//                                                                      "admin@" + serverUri2,
//                                                                      false,
//                                                                      "",
//                                                                      1).execute(client);
//        assertTrue(result.isSuccess());
//    }

    /**
     * Test creation of federated shares with remote users
     */
    @Test
    public void testCreateFederatedShareWithNonExistingSharee() {
        RemoteOperationResult result = new CreateShareRemoteOperation(mFullPath2FileToShare,
                                                                      ShareType.FEDERATED,
                                                                      "no_exist@" + serverUri2,
                                                                      false,
                                                                      "",
                                                                      31).execute(client);

        assertFalse("sharee doesn't exist in an existing remote server", result.isSuccess());
        assertEquals("sharee doesn't exist in an existing remote server, forbidden",
                     RemoteOperationResult.ResultCode.SHARE_FORBIDDEN, result.getCode());

        // error message from server as part of the result
        // TODO verify error message
        assertTrue("sharee doesn't exist in an existing remote server, no error message",
                   result.getData().size() == 1 && result.getData().get(0) instanceof String);

    }

    /**
     * Test creation of federated shares with remote users
     */
    @Test
    public void testCreateFederatedShareWithNonExistingRemoteServer() {
        RemoteOperationResult result = new CreateShareRemoteOperation(mFullPath2FileToShare,
                                                                      ShareType.FEDERATED,
                                                                      "no_exist",
                                                                      false,
                                                                      "",
                                                                      31).execute(client);
        assertFalse(result.isSuccess());
        // TODO expected:<SHARE_WRONG_PARAMETER> but was:<SHARE_FORBIDDEN>
        assertEquals("remote server doesn't exist", ResultCode.SHARE_FORBIDDEN, result.getCode());

        // error message from server as part of the result
        // TODO verify error message
        assertTrue("remote server doesn't exist, no error message",
                   result.getData().size() == 1 && result.getData().get(0) instanceof String);
    }

    /**
     * Test creation of federated shares with remote users
     */
    @Test
    public void testCreateFederatedShareWithNonExistingFile() {
        RemoteOperationResult result = new CreateShareRemoteOperation(mFullPath2NonExistentFile,
                                                                      ShareType.FEDERATED,
                                                                      "admin@" + serverUri2,
                                                                      false,
                                                                      "",
                                                                      31).execute(client);

        assertFalse("file doesn't exist", result.isSuccess());
        assertEquals("file doesn't exist", ResultCode.FILE_NOT_FOUND, result.getCode());
    }
}
