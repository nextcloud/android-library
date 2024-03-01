/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019-2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android;

import static org.junit.Assert.assertTrue;

import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.RemoveFileRemoteOperation;
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Class to test Delete a File Operation
 */
public class DeleteFileIT extends AbstractIT {
    /* Folder data to delete. */
    private static final String FOLDER_PATH = "/folderToDelete";

    /* File to delete. */
    private static final String FILE_PATH = "/fileToDelete.txt";

    private String mFullPath2Folder;
    private String mFullPath2File;

    @Before
    public void setUp() throws Exception {
        mFullPath2Folder = baseFolderPath + FOLDER_PATH;
        mFullPath2File = baseFolderPath + FILE_PATH;

        RemoteOperationResult result = new CreateFolderRemoteOperation(mFullPath2Folder, true).execute(client);
        assertTrue("Error creating folder" + mFullPath2Folder + ": " + result, result.isSuccess());

        File textFile = getFile(ASSETS__TEXT_FILE_NAME);
        result = new UploadFileRemoteOperation(
                textFile.getAbsolutePath(),
                mFullPath2File, "txt/plain",
                System.currentTimeMillis() / MILLI_TO_SECOND
        ).execute(client);

        assertTrue("Error uploading file " + textFile.getAbsolutePath() + ": " + result, result.isSuccess());
    }

    /**
     * Test Remove Folder
     */
    @Test
    public void testRemoveFolder() {
        RemoteOperationResult result = new RemoveFileRemoteOperation(mFullPath2Folder).execute(client);
        assertTrue(result.isSuccess());
    }

    /**
     * Test Remove File
     */
    @Test
    public void testRemoveFile() {
        RemoteOperationResult result = new RemoveFileRemoteOperation(mFullPath2File).execute(client);
        assertTrue(result.isSuccess());
    }

    @After
    public void deleteFixtures() {
        RemoteOperationResult result = new RemoveFileRemoteOperation(baseFolderPath).execute(client);

        assertTrue("Error removing folder " + baseFolderPath + ": " + result, result.isSuccess());
    }
}
