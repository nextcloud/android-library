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
import com.owncloud.android.lib.resources.files.RemoteFile;
import com.owncloud.android.lib.testclient.TestActivity;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Class to test Download File Operation
 *
 * @author masensio
 * @author David A. Velasco
 */

public class DownloadFileTest extends AbstractIT {
    private static final String LOG_TAG = DownloadFileTest.class.getCanonicalName();

    /* Files to download. These files must exist on the account */
    private static final String IMAGE_PATH = "/fileToDownload.png";
    private static final String IMAGE_PATH_WITH_SPECIAL_CHARS = "/@file@download.png";
    private static final String IMAGE_NOT_FOUND = "/fileNotFound.png";

    private String mFullPath2Image;
    private String mFullPath2ImageWitSpecialChars;
    private String mFullPath2ImageNotFound;
    private String mDownloadedFilePath;

//    @Override
//    protected void setUp() throws Exception {
//        super.setUp();
//        setActivityInitialTouchMode(false);
//        mDownloadedFilePath = null;
//        mFullPath2Image = mBaseFolderPath + IMAGE_PATH;
//        mFullPath2ImageWitSpecialChars = mBaseFolderPath + IMAGE_PATH_WITH_SPECIAL_CHARS;
//        mFullPath2ImageNotFound = mBaseFolderPath + IMAGE_NOT_FOUND;
//
//        File imageFile = getFile(TestActivity.ASSETS__IMAGE_FILE_NAME);
//
//        RemoteOperationResult result = mActivity.uploadFile(imageFile.getAbsolutePath(), mFullPath2Image,
//                "image/png", null);
//
//        if (!result.isSuccess()) {
//            Utils.logAndThrow(LOG_TAG, result);
//        }
//
//        result = mActivity.uploadFile(imageFile.getAbsolutePath(), mFullPath2ImageWitSpecialChars, "image/png", null);
//        if (!result.isSuccess()) {
//            Utils.logAndThrow(LOG_TAG, result);
//        }
//
//        result = mActivity.removeFile(mFullPath2ImageNotFound);
//        if (!result.isSuccess() && result.getCode() != ResultCode.FILE_NOT_FOUND) {
//            Utils.logAndThrow(LOG_TAG, result);
//        }
//    }

    /**
     * Test Download a File
     */
    @Test
    public void testDownloadFile() throws FileNotFoundException {
        RemoteOperationResult result = mActivity.downloadFile(new RemoteFile(mFullPath2Image), "");
        mDownloadedFilePath = mFullPath2Image;
        assertTrue(result.isSuccess());
        // TODO some checks involving the local file
    }

    /**
     * Test Download a File with special chars
     */
    public void testDownloadFileSpecialChars() throws FileNotFoundException {
        RemoteOperationResult result = mActivity.downloadFile(new RemoteFile(mFullPath2ImageWitSpecialChars), "");
        mDownloadedFilePath = mFullPath2ImageWitSpecialChars;
        assertTrue(result.isSuccess());
        // TODO some checks involving the local file
    }

    /**
     * Test Download a Not Found File
     */
    public void testDownloadFileNotFound() throws FileNotFoundException {
        RemoteOperationResult result = mActivity.downloadFile(new RemoteFile(mFullPath2ImageNotFound), "");
        assertFalse(result.isSuccess());
    }

    @Override
    protected void tearDown() throws Exception {
        if (mDownloadedFilePath != null) {
            RemoteOperationResult removeResult = mActivity.removeFile(mDownloadedFilePath);
            if (!removeResult.isSuccess()) {
                Utils.logAndThrow(LOG_TAG, removeResult);
            }
        }

        File[] files = mActivity.getFilesDir().listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                FileUtils.deleteDirectory(file);
            } else if (!file.delete()) {
                throw new FileNotFoundException(file.getAbsolutePath());
            }
        }
        super.tearDown();
    }
}
