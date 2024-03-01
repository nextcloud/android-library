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
 * Class to test Update File Operation
 *
 * @author masensio
 * @author David A. Velasco
 */

public class UploadFileTest extends RemoteTest {

    private static final String LOG_TAG = UploadFileTest.class.getCanonicalName();

    private static final String UPLOAD_PATH = "/uploadedImage.png";

    private static final String CHUNKED_UPLOAD_PATH = "/uploadedVideo.MP4";

    private static final String FILE_NOT_FOUND_PATH = "/notFoundShouldNotBeHere.png";

    private File mFileToUpload, mFileToUploadWithChunks;
    private String mUploadedFilePath;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        mUploadedFilePath = null;

        mFileToUpload = getFile(TestActivity.ASSETS__IMAGE_FILE_NAME);
        mFileToUploadWithChunks = getFile(TestActivity.ASSETS__VIDEO_FILE_NAME);
    }

    /**
     * Test Upload File without chunks
     */
    public void testUploadFile() {

        String fullPath2Upload = mBaseFolderPath + UPLOAD_PATH;
        RemoteOperationResult result = mActivity.uploadFile(mFileToUpload.getAbsolutePath(), fullPath2Upload,
                "image/png", null);
        mUploadedFilePath = fullPath2Upload;
        assertTrue(result.isSuccess());
    }

    /**
     * Test Upload File with chunks
     */
    public void testUploadFileWithChunks() {

        String fullPath2Upload = mBaseFolderPath + CHUNKED_UPLOAD_PATH;
        RemoteOperationResult result = mActivity.uploadFile(mFileToUploadWithChunks.getAbsolutePath(), fullPath2Upload,
                "video/mp4", null);
        mUploadedFilePath = fullPath2Upload;
        assertTrue(result.isSuccess());
    }

    /**
     * Test Upload Not Found File
     */
    public void testUploadFileNotFound() {
        String fullPath2Upload = mBaseFolderPath + FILE_NOT_FOUND_PATH;
        RemoteOperationResult result = mActivity.uploadFile(FILE_NOT_FOUND_PATH, fullPath2Upload, "image/png", null);
        mUploadedFilePath = fullPath2Upload;
        assertFalse(result.isSuccess());
    }

    @Override
    protected void tearDown() throws Exception {
        if (mUploadedFilePath != null) {
            RemoteOperationResult removeResult = mActivity.removeFile(mUploadedFilePath);
            if (!removeResult.isSuccess()) {
                Utils.logAndThrow(LOG_TAG, removeResult);
            }
        }
        super.tearDown();
    }
}
