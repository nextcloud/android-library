/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android;

import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.testclient.TestActivity;

import java.io.File;

/**
 * Class to test Read Folder Operation
 *
 * @author masensio
 * @author David A. Velasco
 */

public class ReadFolderTest extends RemoteTest {

    private static final String LOG_TAG = ReadFolderTest.class.getCanonicalName();

    private static final String FOLDER_PATH = "/folderToRead";
    private static final String[] FILE_PATHS = {
            FOLDER_PATH + "/file1.txt",
            FOLDER_PATH + "/file2.txt",
            FOLDER_PATH + "/file3.txt",
    };


    private String mFullPathToFolder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        mFullPathToFolder = mBaseFolderPath + FOLDER_PATH;

        File textFile = getFile(TestActivity.ASSETS__TEXT_FILE_NAME);
        RemoteOperationResult result = mActivity.createFolder(mFullPathToFolder, true);
        if (result.isSuccess()) {
            for (int i = 0; i < FILE_PATHS.length && result.isSuccess(); i++) {
                result = mActivity.uploadFile(textFile.getAbsolutePath(), mBaseFolderPath + FILE_PATHS[i], "txt/plain",
                        null);
            }
        }

        if (!result.isSuccess()) {
            Utils.logAndThrow(LOG_TAG, result);
        }
    }

    /**
     * Test Read Folder
     */
    public void testReadFolder() {
        RemoteOperationResult result = mActivity.readFile(mFullPathToFolder);
        assertTrue(result.isSuccess());
        assertTrue(result.getData() != null && result.getData().size() > 1);
        assertTrue(result.getData().size() == 4);
        // TODO assert more properties about the result
    }


    @Override
    protected void tearDown() throws Exception {
        RemoteOperationResult removeResult = mActivity.removeFile(mFullPathToFolder);
        if (!removeResult.isSuccess()) {
            Utils.logAndThrow(LOG_TAG, removeResult);
        }

        super.tearDown();
    }

}
