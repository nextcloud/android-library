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

import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.RemoveFileRemoteOperation;
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Class to test Delete a File Operation
 */

public class DeleteFileTest extends AbstractIT {
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
        result = new UploadFileRemoteOperation(textFile.getAbsolutePath(), mFullPath2File, "txt/plain",
                                               String.valueOf(System.currentTimeMillis() / 1000)).execute(client);

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
