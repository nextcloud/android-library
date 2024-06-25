/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019-2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode;
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation;
import com.owncloud.android.lib.resources.shares.CreateShareRemoteOperation;
import com.owncloud.android.lib.resources.shares.ShareType;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Test create share
 */
public class CreateShareIT extends AbstractIT {
    private static final String NON_EXISTENT_FILE = "/nonExistentFile.txt";
    private static final String serverUri2 = "server2";

    private String mFullPath2FileToShare;
    private String mFullPath2NonExistentFile;

    @Before
    public void setUp() throws Exception {
        mFullPath2FileToShare = ASSETS__TEXT_FILE_NAME;
        mFullPath2NonExistentFile = NON_EXISTENT_FILE;

        File textFile = getFile(ASSETS__TEXT_FILE_NAME);
        RemoteOperationResult result = new UploadFileRemoteOperation(
                textFile.getAbsolutePath(),
                mFullPath2FileToShare,
                "txt/plain",
                System.currentTimeMillis() / MILLI_TO_SECOND
        ).execute(client);

        assertTrue("Error uploading file " + textFile + ": " + result, result.isSuccess());
    }

    @Test
    public void testCreatePublicShareSuccessful() {
        RemoteOperationResult result = new CreateShareRemoteOperation(
                mFullPath2FileToShare,
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
