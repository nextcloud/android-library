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
package com.owncloud.android;

import android.util.Log;

import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode;
import com.owncloud.android.lib.resources.files.MoveRemoteFileOperation;
import com.owncloud.android.lib.resources.status.OwnCloudVersion;
import com.owncloud.android.lib.testclient.TestActivity;

import org.apache.commons.httpclient.HttpStatus;

import java.io.File;

/**
 * Class to test MoveRemoteFileOperation
 * <p>
 * With this TestCase we are experimenting a bit to improve the test suite design, in two aspects:
 * <p>
 * - Reduce the dependency from the set of test cases on the "test project" needed to
 * have an instrumented APK to install in the device, as required by the testing framework
 * provided by Android. To get there, this class avoids calling TestActivity methods in the test
 * method.
 * <p>
 * - Reduce the impact of creating a remote fixture over the Internet, while the structure of the
 * TestCase is kept easy to maintain. To get this, all the tests are done in a single test method,
 * granting this way that setUp and tearDown are run only once.
 *
 * @author David A. Velasco
 */

//public class MoveFileTest extends AndroidTestCase {
public class MoveFileTest extends RemoteTest {

    private static final String LOG_TAG = MoveFileTest.class.getCanonicalName();

    /// Paths to files and folders in fixture

    private static final String SRC_BASE_FOLDER = "/src/";
    private static final String TARGET_BASE_FOLDER = "/target/";
    private static final String NO_FILE = "nofile.txt";
    private static final String FILE1 = "file1.txt";
    private static final String FILE2 = "file2.txt";
    private static final String FILE3 = "file3.txt";
    private static final String FILE4 = "file4.txt";
    private static final String FILE5 = "file5.txt";
    private static final String FILE6 = "file6.txt";
    private static final String FILE7 = "file7.txt";
    private static final String EMPTY = "empty/";
    private static final String NO_FOLDER = "nofolder/";
    private static final String FOLDER1 = "folder1/";
    private static final String FOLDER2 = "folder2/";
    private static final String FOLDER3 = "folder3/";
    private static final String FOLDER4 = "folder4/";

    private static final String SRC_PATH_TO_FILE_1 = SRC_BASE_FOLDER + FILE1;
    private static final String TARGET_PATH_TO_FILE_1 = TARGET_BASE_FOLDER + FILE1;

    private static final String SRC_PATH_TO_FILE_2 = SRC_BASE_FOLDER + FILE2;
    private static final String TARGET_PATH_TO_FILE_2_RENAMED = TARGET_BASE_FOLDER + "renamed_" + FILE2;

    private static final String SRC_PATH_TO_FILE_3 = SRC_BASE_FOLDER + FILE3;
    private static final String SRC_PATH_TO_FILE_3_RENAMED = SRC_BASE_FOLDER + "renamed_" + FILE3;

    private static final String SRC_PATH_TO_FILE_4 = SRC_BASE_FOLDER + FILE4;

    private static final String SRC_PATH_TO_FILE_5 = SRC_BASE_FOLDER + FILE5;

    private static final String SRC_PATH_TO_FILE_6 = SRC_BASE_FOLDER + FILE6;

    private static final String SRC_PATH_TO_FILE_7 = SRC_BASE_FOLDER + FILE7;

    private static final String SRC_PATH_TO_NON_EXISTENT_FILE = SRC_BASE_FOLDER + NO_FILE;

    private static final String SRC_PATH_TO_EMPTY_FOLDER = SRC_BASE_FOLDER + EMPTY;
    private static final String TARGET_PATH_TO_EMPTY_FOLDER = TARGET_BASE_FOLDER + EMPTY;

    private static final String SRC_PATH_TO_FULL_FOLDER_1 = SRC_BASE_FOLDER + FOLDER1;
    private static final String TARGET_PATH_TO_FULL_FOLDER_1 = TARGET_BASE_FOLDER + FOLDER1;

    private static final String SRC_PATH_TO_FULL_FOLDER_2 = SRC_BASE_FOLDER + FOLDER2;

    private static final String TARGET_PATH_TO_FULL_FOLDER_2_RENAMED = TARGET_BASE_FOLDER + "renamed_" + FOLDER2;

    private static final String SRC_PATH_TO_FULL_FOLDER_3 = SRC_BASE_FOLDER + FOLDER3;
    private static final String SRC_PATH_TO_FULL_FOLDER_4 = SRC_BASE_FOLDER + FOLDER4;

    private static final String SRC_PATH_TO_FULL_FOLDER_3_RENAMED = SRC_BASE_FOLDER + "renamed_" + FOLDER3;

    private static final String TARGET_PATH_RENAMED_WITH_INVALID_CHARS = SRC_BASE_FOLDER + "renamed:??_" + FILE6;

    private static final String TARGET_PATH_TO_ALREADY_EXISTENT_EMPTY_FOLDER_4 = TARGET_BASE_FOLDER + FOLDER4;

    private static final String TARGET_PATH_TO_NON_EXISTENT_FILE = TARGET_BASE_FOLDER + NO_FILE;

    private static final String TARGET_PATH_TO_FILE_5_INTO_NON_EXISTENT_FOLDER = TARGET_BASE_FOLDER + NO_FOLDER + FILE5;

    private static final String TARGET_PATH_TO_ALREADY_EXISTENT_FILE_7 = TARGET_BASE_FOLDER + FILE7;

    private static final String[] FOLDERS_IN_FIXTURE = {
            SRC_PATH_TO_EMPTY_FOLDER,

            SRC_PATH_TO_FULL_FOLDER_1,
            SRC_PATH_TO_FULL_FOLDER_1 + FOLDER1,
            SRC_PATH_TO_FULL_FOLDER_1 + FOLDER2,
            SRC_PATH_TO_FULL_FOLDER_1 + FOLDER2 + FOLDER1,
            SRC_PATH_TO_FULL_FOLDER_1 + FOLDER2 + FOLDER2,

            SRC_PATH_TO_FULL_FOLDER_2,
            SRC_PATH_TO_FULL_FOLDER_2 + FOLDER1,
            SRC_PATH_TO_FULL_FOLDER_2 + FOLDER2,
            SRC_PATH_TO_FULL_FOLDER_2 + FOLDER2 + FOLDER1,
            SRC_PATH_TO_FULL_FOLDER_2 + FOLDER2 + FOLDER2,

            SRC_PATH_TO_FULL_FOLDER_3,
            SRC_PATH_TO_FULL_FOLDER_3 + FOLDER1,
            SRC_PATH_TO_FULL_FOLDER_3 + FOLDER2,
            SRC_PATH_TO_FULL_FOLDER_3 + FOLDER2 + FOLDER1,
            SRC_PATH_TO_FULL_FOLDER_3 + FOLDER2 + FOLDER2,

            SRC_PATH_TO_FULL_FOLDER_4,
            SRC_PATH_TO_FULL_FOLDER_4 + FOLDER1,
            SRC_PATH_TO_FULL_FOLDER_4 + FOLDER2,
            SRC_PATH_TO_FULL_FOLDER_4 + FOLDER2 + FOLDER1,
            SRC_PATH_TO_FULL_FOLDER_4 + FOLDER2 + FOLDER2,

            TARGET_BASE_FOLDER,
            TARGET_PATH_TO_ALREADY_EXISTENT_EMPTY_FOLDER_4
    };

    private static final String[] FILES_IN_FIXTURE = {
            SRC_PATH_TO_FILE_1,
            SRC_PATH_TO_FILE_2,
            SRC_PATH_TO_FILE_3,
            SRC_PATH_TO_FILE_4,
            SRC_PATH_TO_FILE_5,

            SRC_PATH_TO_FULL_FOLDER_1 + FILE1,
            SRC_PATH_TO_FULL_FOLDER_1 + FOLDER2 + FILE1,
            SRC_PATH_TO_FULL_FOLDER_1 + FOLDER2 + FILE2,
            SRC_PATH_TO_FULL_FOLDER_1 + FOLDER2 + FOLDER2 + FILE2,

            SRC_PATH_TO_FULL_FOLDER_2 + FILE1,
            SRC_PATH_TO_FULL_FOLDER_2 + FOLDER2 + FILE1,
            SRC_PATH_TO_FULL_FOLDER_2 + FOLDER2 + FILE2,
            SRC_PATH_TO_FULL_FOLDER_2 + FOLDER2 + FOLDER2 + FILE2,

            SRC_PATH_TO_FULL_FOLDER_3 + FILE1,
            SRC_PATH_TO_FULL_FOLDER_3 + FOLDER2 + FILE1,
            SRC_PATH_TO_FULL_FOLDER_3 + FOLDER2 + FILE2,
            SRC_PATH_TO_FULL_FOLDER_3 + FOLDER2 + FOLDER2 + FILE2,

            SRC_PATH_TO_FULL_FOLDER_4 + FILE1,
            SRC_PATH_TO_FULL_FOLDER_4 + FOLDER2 + FILE1,
            SRC_PATH_TO_FULL_FOLDER_4 + FOLDER2 + FILE2,
            SRC_PATH_TO_FULL_FOLDER_4 + FOLDER2 + FOLDER2 + FILE2,

            TARGET_PATH_TO_ALREADY_EXISTENT_FILE_7
    };

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Log.v(LOG_TAG, "Setting up the remote fixture...");

        RemoteOperationResult result;
        for (String folderPath : FOLDERS_IN_FIXTURE) {
            result = mActivity.createFolder(mBaseFolderPath + folderPath, true);
            if (!result.isSuccess()) {
                Utils.logAndThrow(LOG_TAG, result);
            }
        }

        File txtFile = getFile(TestActivity.ASSETS__TEXT_FILE_NAME);
        for (String filePath : FILES_IN_FIXTURE) {
            result = mActivity.uploadFile(txtFile.getAbsolutePath(), mBaseFolderPath + filePath, "txt/plain", null);
            if (!result.isSuccess()) {
                Utils.logAndThrow(LOG_TAG, result);
            }
        }

        Log.v(LOG_TAG, "Remote fixture created.");
    }


    /**
     * Test move folder
     */
    public void testMoveRemoteFileOperation() {
        Log.v(LOG_TAG, "testMoveFolder in");

        /// successful cases

        // move file
        MoveRemoteFileOperation moveOperation = new MoveRemoteFileOperation(
                mBaseFolderPath + SRC_PATH_TO_FILE_1,
                mBaseFolderPath + TARGET_PATH_TO_FILE_1,
                false
        );
        RemoteOperationResult result = moveOperation.execute(mClient);
        assertTrue("move file", result.isSuccess());

        // move & rename file, different location
        moveOperation = new MoveRemoteFileOperation(
                mBaseFolderPath + SRC_PATH_TO_FILE_2,
                mBaseFolderPath + TARGET_PATH_TO_FILE_2_RENAMED,
                false
        );
        result = moveOperation.execute(mClient);
        assertTrue("move & rename file, different location", result.isSuccess());

        // move & rename file, same location (rename file)
        moveOperation = new MoveRemoteFileOperation(
                mBaseFolderPath + SRC_PATH_TO_FILE_3,
                mBaseFolderPath + SRC_PATH_TO_FILE_3_RENAMED,
                false
        );
        result = moveOperation.execute(mClient);
        assertTrue("move & rename file, same location (rename file)", result.isSuccess());

        // move empty folder
        moveOperation = new MoveRemoteFileOperation(
                mBaseFolderPath + SRC_PATH_TO_EMPTY_FOLDER,
                mBaseFolderPath + TARGET_PATH_TO_EMPTY_FOLDER,
                false
        );
        result = moveOperation.execute(mClient);
        assertTrue("move empty folder", result.isSuccess());

        // move non-empty folder
        moveOperation = new MoveRemoteFileOperation(
                mBaseFolderPath + SRC_PATH_TO_FULL_FOLDER_1,
                mBaseFolderPath + TARGET_PATH_TO_FULL_FOLDER_1,
                false
        );
        result = moveOperation.execute(mClient);
        assertTrue("move non-empty folder", result.isSuccess());

        // move & rename folder, different location
        moveOperation = new MoveRemoteFileOperation(
                mBaseFolderPath + SRC_PATH_TO_FULL_FOLDER_2,
                mBaseFolderPath + TARGET_PATH_TO_FULL_FOLDER_2_RENAMED,
                false
        );
        result = moveOperation.execute(mClient);
        assertTrue("move & rename folder, different location", result.isSuccess());

        // move & rename folder, same location (rename folder)
        moveOperation = new MoveRemoteFileOperation(
                mBaseFolderPath + SRC_PATH_TO_FULL_FOLDER_3,
                mBaseFolderPath + SRC_PATH_TO_FULL_FOLDER_3_RENAMED,
                false
        );
        result = moveOperation.execute(mClient);
        assertTrue("move & rename folder, same location (rename folder)", result.isSuccess());

        // move for nothing (success, but no interaction with network)
        moveOperation = new MoveRemoteFileOperation(
                mBaseFolderPath + SRC_PATH_TO_FILE_4,
                mBaseFolderPath + SRC_PATH_TO_FILE_4,
                false
        );
        result = moveOperation.execute(mClient);
        assertTrue("move for nothing (success, but no interaction with network)", result.isSuccess());

        // move overwriting
        moveOperation = new MoveRemoteFileOperation(
                mBaseFolderPath + SRC_PATH_TO_FULL_FOLDER_4,
                mBaseFolderPath + TARGET_PATH_TO_ALREADY_EXISTENT_EMPTY_FOLDER_4,
                true
        );
        result = moveOperation.execute(mClient);
        assertTrue("move overwriting", result.isSuccess());


        /// Failed cases

        // file to move does not exist
        moveOperation = new MoveRemoteFileOperation(
                mBaseFolderPath + SRC_PATH_TO_NON_EXISTENT_FILE,
                mBaseFolderPath + TARGET_PATH_TO_NON_EXISTENT_FILE,
                false
        );
        result = moveOperation.execute(mClient);
        assertTrue("file to move does not exist", result.getCode() == ResultCode.FILE_NOT_FOUND);

        // folder to move into does no exist
        moveOperation = new MoveRemoteFileOperation(
                mBaseFolderPath + SRC_PATH_TO_FILE_5,
                mBaseFolderPath + TARGET_PATH_TO_FILE_5_INTO_NON_EXISTENT_FOLDER,
                false
        );
        result = moveOperation.execute(mClient);
        assertTrue("folder to move into does no exist", result.getHttpCode() == HttpStatus.SC_CONFLICT);

        // target location (renaming) has invalid characters
        mActivity.getClient().setOwnCloudVersion(null);
        moveOperation = new MoveRemoteFileOperation(
                mBaseFolderPath + SRC_PATH_TO_FILE_6,
                mBaseFolderPath + TARGET_PATH_RENAMED_WITH_INVALID_CHARS,
                false
        );
        result = moveOperation.execute(mClient);
        assertTrue("target location (renaming) has invalid characters", result.getCode() == ResultCode.INVALID_CHARACTER_IN_NAME);
        mActivity.getClient().setOwnCloudVersion(OwnCloudVersion.nextcloud_10);

        // name collision
        moveOperation = new MoveRemoteFileOperation(
                mBaseFolderPath + SRC_PATH_TO_FILE_7,
                mBaseFolderPath + TARGET_PATH_TO_ALREADY_EXISTENT_FILE_7,
                false
        );
        result = moveOperation.execute(mClient);
        assertTrue("name collision", result.getCode() == ResultCode.INVALID_OVERWRITE);

        // move a folder into a descendant
        moveOperation = new MoveRemoteFileOperation(
                mBaseFolderPath + SRC_BASE_FOLDER,
                mBaseFolderPath + SRC_PATH_TO_EMPTY_FOLDER,
                false
        );
        result = moveOperation.execute(mClient);
        assertTrue("move a folder into a descendant", result.getCode() == ResultCode.INVALID_MOVE_INTO_DESCENDANT);
    }

    @Override
    protected void tearDown() throws Exception {
        Log.v(LOG_TAG, "Deleting remote fixture...");

        String[] mPathsToCleanUp = {
                mBaseFolderPath + SRC_BASE_FOLDER,
                mBaseFolderPath + TARGET_BASE_FOLDER
        };

        for (String path : mPathsToCleanUp) {
            RemoteOperationResult removeResult = mActivity.removeFile(path);
            if (!removeResult.isSuccess()) {
                Utils.logAndThrow(LOG_TAG, removeResult);
            }
        }

        super.tearDown();

        Log.v(LOG_TAG, "Remote fixture delete.");
    }
}
