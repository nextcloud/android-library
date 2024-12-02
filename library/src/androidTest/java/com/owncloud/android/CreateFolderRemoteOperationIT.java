/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019-2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android;

import static com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode.FOLDER_ALREADY_EXISTS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.nextcloud.test.RandomStringGenerator;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.ExistenceCheckRemoteOperation;
import com.owncloud.android.lib.resources.files.ReadFileRemoteOperation;
import com.owncloud.android.lib.resources.files.RemoveFileRemoteOperation;
import com.owncloud.android.lib.resources.files.model.RemoteFile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class to test Create Folder Operation
 */
public class CreateFolderRemoteOperationIT extends AbstractIT {
    private static final String FOLDER_PATH_BASE = "/testCreateFolder";
    private static final int TAG_LENGTH = 10;

    private final List<String> mCreatedFolderPaths;
    private String mFullPath2FolderBase;

    public CreateFolderRemoteOperationIT() {
        super();
        mCreatedFolderPaths = new ArrayList<>();
    }

    @Before
    public void setUp() {
        mCreatedFolderPaths.clear();
        mFullPath2FolderBase = baseFolderPath + FOLDER_PATH_BASE;

        mCreatedFolderPaths.add(mFullPath2FolderBase);
    }

    /**
     * Test Create Folder
     */
    @Test
    public void testCreateFolder() {
        String remotePath = mFullPath2FolderBase;
        mCreatedFolderPaths.add(remotePath);
        RemoteOperationResult<String> result = new CreateFolderRemoteOperation(remotePath, true).execute(nextcloudClient);
        assertTrue(result.isSuccess());

        // Create Subfolder
        remotePath = mFullPath2FolderBase + FOLDER_PATH_BASE;
        mCreatedFolderPaths.add(remotePath);
        result = new CreateFolderRemoteOperation(remotePath, true).execute(nextcloudClient);
        assertTrue(result.isSuccess());
    }

    /**
     * Test duplicate Folder
     */
    @Test
    public void testCreateDuplicateFolder() {
        String remotePath = mFullPath2FolderBase + "duplicateFolder";
        mCreatedFolderPaths.add(remotePath);
        RemoteOperationResult<String> result = new CreateFolderRemoteOperation(remotePath, true).execute(nextcloudClient);
        assertTrue(result.isSuccess());

        // Create folder again
        remotePath = mFullPath2FolderBase + "duplicateFolder";
        mCreatedFolderPaths.add(remotePath);
        result = new CreateFolderRemoteOperation(remotePath, true).execute(nextcloudClient);
        assertFalse(result.isSuccess());
        assertEquals(FOLDER_ALREADY_EXISTS, result.getCode());
    }

    @Test
    public void testFileID() {
        String remotePath = mFullPath2FolderBase + "/" + RandomStringGenerator.make(TAG_LENGTH);
        mCreatedFolderPaths.add(remotePath);
        RemoteOperationResult<String> result = new CreateFolderRemoteOperation(remotePath, true).execute(nextcloudClient);
        assertTrue(result.isSuccess());

        RemoteOperationResult<RemoteFile> readResult = new ReadFileRemoteOperation(remotePath).execute(nextcloudClient);
        assertTrue(readResult.isSuccess());

        String remoteId = readResult.getResultData().getRemoteId();
        assertEquals(result.getResultData(), remoteId);
    }

    /**
     * Test to create folder with special characters: /  \  < >  :  "  |  ?
     * > oc8.1 no characters are forbidden
     */
    @Test
    public void testCreateFolderSpecialCharactersOnNewVersion() {
        String remotePath = mFullPath2FolderBase + "_<";
        RemoteOperationResult<String> result = new CreateFolderRemoteOperation(remotePath, true).execute(nextcloudClient);
        assertTrue("Remote path: " + remotePath, result.isSuccess());

        remotePath = mFullPath2FolderBase + "_>";
        result = new CreateFolderRemoteOperation(remotePath, true).execute(nextcloudClient);
        assertTrue("Remote path: " + remotePath, result.isSuccess());

        remotePath = mFullPath2FolderBase + "_:";
        result = new CreateFolderRemoteOperation(remotePath, true).execute(nextcloudClient);
        assertTrue("Remote path: " + remotePath, result.isSuccess());

        remotePath = mFullPath2FolderBase + "_\"";
        result = new CreateFolderRemoteOperation(remotePath, true).execute(nextcloudClient);
        assertTrue("Remote path: " + remotePath, result.isSuccess());

        remotePath = mFullPath2FolderBase + "_|";
        result = new CreateFolderRemoteOperation(remotePath, true).execute(nextcloudClient);
        assertTrue("Remote path: " + remotePath, result.isSuccess());

        remotePath = mFullPath2FolderBase + "_?";
        result = new CreateFolderRemoteOperation(remotePath, true).execute(nextcloudClient);
        assertTrue("Remote path: " + remotePath, result.isSuccess());

        remotePath = mFullPath2FolderBase + "_*";
        result = new CreateFolderRemoteOperation(remotePath, true).execute(nextcloudClient);
        assertTrue("Remote path: " + remotePath, result.isSuccess());
    }


    @After
    public void tearDown() {
        Iterator<String> it = mCreatedFolderPaths.iterator();
        RemoteOperationResult removeResult;
        while (it.hasNext()) {
            String path = it.next();

            ExistenceCheckRemoteOperation existenceCheckOperation = new ExistenceCheckRemoteOperation(path, false);
            RemoteOperationResult result = existenceCheckOperation.execute(client);

            if (result.isSuccess()) {
                removeResult = new RemoveFileRemoteOperation(path).execute(nextcloudClient);

                assertTrue("Error removing folder " + path + ":" + removeResult, removeResult.isSuccess());
            }
        }
    }
}
