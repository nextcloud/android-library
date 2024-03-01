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
 * Class to test Read File Operation
 *
 * @author masensio
 * @author David A. Velasco
 */

public class ReadFileTest extends RemoteTest {

    private static final String LOG_TAG = ReadFileTest.class.getCanonicalName();

    private String FILE_PATH = "/fileToRead.txt";
    private String mFullPath2File;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(false);
        mFullPath2File = mBaseFolderPath + FILE_PATH;

        File textFile = getFile(TestActivity.ASSETS__TEXT_FILE_NAME);
        RemoteOperationResult uploadResult = mActivity.uploadFile(textFile.getAbsolutePath(), mFullPath2File,
                "txt/plain", null);

        if (!uploadResult.isSuccess()) {
            Utils.logAndThrow(LOG_TAG, uploadResult);
        }
    }

    /**
     * Test Read File
     */
    public void testReadFile() {
        RemoteOperationResult result = mActivity.readFile(mFullPath2File);
        assertTrue(result.getData() != null && result.getData().size() == 1);
        assertTrue(result.isSuccess());
        // TODO check more properties of the result
    }

    @Override
    protected void tearDown() throws Exception {
        RemoteOperationResult removeResult = mActivity.removeFile(mFullPath2File);
        if (!removeResult.isSuccess()) {
            Utils.logAndThrow(LOG_TAG, removeResult);
        }

        super.tearDown();
    }
}
