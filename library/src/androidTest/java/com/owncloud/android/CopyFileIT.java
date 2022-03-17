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
import com.owncloud.android.lib.resources.files.CopyFileRemoteOperation;
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.RemoveFileRemoteOperation;
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Class to test CopyRemoteFileOperation
 */

public class CopyFileIT extends AbstractIT {

    private static final String LOG_TAG = CopyFileIT.class.getCanonicalName();

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

    @Before
    public void createFixtures() throws Exception {
        Log.v(LOG_TAG, "Setting up the remote fixture...");

        RemoteOperationResult result;
        for (String folderPath : FOLDERS_IN_FIXTURE) {
            result = new CreateFolderRemoteOperation(folderPath, true).execute(client);

            assertTrue("Error creating folder" + folderPath + ": " + result, result.isSuccess());
        }

        File txtFile = getFile(ASSETS__TEXT_FILE_NAME);

        for (String filePath : FILES_IN_FIXTURE) {
            result = new UploadFileRemoteOperation(txtFile.getAbsolutePath(), filePath, "txt/plain", String.valueOf(System.currentTimeMillis() / 1000)).execute(client);

            assertTrue("Error uploading file " + filePath + ": " + result, result.isSuccess());
        }

        Log.v(LOG_TAG, "Remote fixture created.");
    }

    @After
    public void deleteFixtures() {
        String[] folders = new String[]{SRC_BASE_FOLDER, TARGET_BASE_FOLDER};

        for (String folder : folders) {
            RemoteOperationResult result = new RemoveFileRemoteOperation(folder).execute(client);

            assertTrue("Error removing folder " + folder + ": " + result, result.isSuccess());
        }
    }

    /**
     * Test copy folder
     */
    @Test
    public void testCopyRemoteFileOperation() {
        Log.v(LOG_TAG, "testCopyFolder in");

        /// successful cases

        // copy file
        CopyFileRemoteOperation copyOperation = new CopyFileRemoteOperation(SRC_PATH_TO_FILE_1, TARGET_PATH_TO_FILE_1, false);
        RemoteOperationResult result = copyOperation.execute(client);
        assertTrue("copy file: " + result, result.isSuccess());

        // copy & rename file, different location
        copyOperation = new CopyFileRemoteOperation(SRC_PATH_TO_FILE_2, TARGET_PATH_TO_FILE_2_RENAMED, false);
        result = copyOperation.execute(client);
        assertTrue("copy & rename file, different location", result.isSuccess());

        // copy & rename file, same location (rename file)
        copyOperation = new CopyFileRemoteOperation(SRC_PATH_TO_FILE_3, SRC_PATH_TO_FILE_3_RENAMED, false);
        result = copyOperation.execute(client);
        assertTrue("copy & rename file, same location (rename file)", result.isSuccess());

        // copy empty folder
        copyOperation = new CopyFileRemoteOperation(SRC_PATH_TO_EMPTY_FOLDER, TARGET_PATH_TO_EMPTY_FOLDER, false);
        result = copyOperation.execute(client);
        assertTrue("copy empty folder", result.isSuccess());

        // copy non-empty folder
        copyOperation = new CopyFileRemoteOperation(SRC_PATH_TO_FULL_FOLDER_1, TARGET_PATH_TO_FULL_FOLDER_1, false);
        result = copyOperation.execute(client);
        assertTrue("copy non-empty folder", result.isSuccess());

        // copy & rename folder, different location
        copyOperation = new CopyFileRemoteOperation(SRC_PATH_TO_FULL_FOLDER_2, TARGET_PATH_TO_FULL_FOLDER_2_RENAMED,
                                                    false);
        result = copyOperation.execute(client);
        assertTrue("copy & rename folder, different location", result.isSuccess());

        // copy & rename folder, same location (rename folder)
        copyOperation = new CopyFileRemoteOperation(SRC_PATH_TO_FULL_FOLDER_3, SRC_PATH_TO_FULL_FOLDER_3_RENAMED,
                                                    false);
        result = copyOperation.execute(client);
        assertTrue("copy & rename folder, same location (rename folder)", result.isSuccess());

        // copy for nothing (success, but no interaction with network)
        copyOperation = new CopyFileRemoteOperation(SRC_PATH_TO_FILE_4, SRC_PATH_TO_FILE_4, false);
        result = copyOperation.execute(client);
        assertTrue("copy for nothing (success, but no interaction with network)", result.isSuccess());

        // copy overwriting
        copyOperation = new CopyFileRemoteOperation(SRC_PATH_TO_FULL_FOLDER_4,
                                                    TARGET_PATH_TO_ALREADY_EXISTENT_EMPTY_FOLDER_4, true);
        result = copyOperation.execute(client);
        assertTrue("copy overwriting", result.isSuccess());


        /// Failed cases

        // file to copy does not exist
        copyOperation = new CopyFileRemoteOperation(SRC_PATH_TO_NON_EXISTENT_FILE, TARGET_PATH_TO_NON_EXISTENT_FILE,
                                                    false);
        result = copyOperation.execute(client);
        assertSame("file to copy does not exist", result.getCode(), ResultCode.FILE_NOT_FOUND);

        // folder to copy into does no exist
        copyOperation = new CopyFileRemoteOperation(SRC_PATH_TO_FILE_5, TARGET_PATH_TO_FILE_5_INTO_NON_EXISTENT_FOLDER,
                                                    false);
        result = copyOperation.execute(client);
        assertEquals("folder to copy into does no exist", result.getHttpCode(), HttpStatus.SC_CONFLICT);

        // name collision
        copyOperation = new CopyFileRemoteOperation(SRC_PATH_TO_FILE_1, TARGET_PATH_TO_ALREADY_EXISTENT_FILE_7, false);
        result = copyOperation.execute(client);
        assertSame("Name collision", result.getCode(), ResultCode.INVALID_OVERWRITE);

        // copy a folder into a descendant
        copyOperation = new CopyFileRemoteOperation(SRC_BASE_FOLDER, SRC_PATH_TO_EMPTY_FOLDER, false);
        result = copyOperation.execute(client);
        assertSame("Copy a folder into a descendant", result.getCode(), ResultCode.INVALID_COPY_INTO_DESCENDANT);

        // rename folder with invalid filename
        copyOperation = new CopyFileRemoteOperation(SRC_BASE_FOLDER, TARGET_PATH_RENAMED_WITH_INVALID_CHARS, false);
        result = copyOperation.execute(client);
        assertSame("Copy a folder into a descendant", result.getCode(), ResultCode.INVALID_COPY_INTO_DESCENDANT);
    }
}
