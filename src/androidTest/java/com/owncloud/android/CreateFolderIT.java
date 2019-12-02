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
import com.owncloud.android.lib.resources.files.ExistenceCheckRemoteOperation;
import com.owncloud.android.lib.resources.files.RemoveFileRemoteOperation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Class to test Create Folder Operation
 */
public class CreateFolderIT extends AbstractIT {
    private static final String FOLDER_PATH_BASE = "/testCreateFolder";

    private List<String> mCreatedFolderPaths;
    private String mFullPath2FolderBase;

    public CreateFolderIT() {
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
        RemoteOperationResult result = new CreateFolderRemoteOperation(remotePath, true).execute(client);
        assertTrue(result.isSuccess());

        // Create Subfolder
        remotePath = mFullPath2FolderBase + FOLDER_PATH_BASE;
        mCreatedFolderPaths.add(remotePath);
        result = new CreateFolderRemoteOperation(remotePath, true).execute(client);
        assertTrue(result.isSuccess());
    }

    /**
     * Test to create folder with special characters: /  \  < >  :  "  |  ?
     * > oc8.1 no characters are forbidden
     */
    @Test
    public void testCreateFolderSpecialCharactersOnNewVersion() {
        String remotePath = mFullPath2FolderBase + "_<";
        RemoteOperationResult result = new CreateFolderRemoteOperation(remotePath, true).execute(client);
        assertTrue("Remote path: " + remotePath, result.isSuccess());

        remotePath = mFullPath2FolderBase + "_>";
        result = new CreateFolderRemoteOperation(remotePath, true).execute(client);
        assertTrue("Remote path: " + remotePath, result.isSuccess());

        remotePath = mFullPath2FolderBase + "_:";
        result = new CreateFolderRemoteOperation(remotePath, true).execute(client);
        assertTrue("Remote path: " + remotePath, result.isSuccess());

        remotePath = mFullPath2FolderBase + "_\"";
        result = new CreateFolderRemoteOperation(remotePath, true).execute(client);
        assertTrue("Remote path: " + remotePath, result.isSuccess());

        remotePath = mFullPath2FolderBase + "_|";
        result = new CreateFolderRemoteOperation(remotePath, true).execute(client);
        assertTrue("Remote path: " + remotePath, result.isSuccess());

        remotePath = mFullPath2FolderBase + "_?";
        result = new CreateFolderRemoteOperation(remotePath, true).execute(client);
        assertTrue("Remote path: " + remotePath, result.isSuccess());

        remotePath = mFullPath2FolderBase + "_*";
        result = new CreateFolderRemoteOperation(remotePath, true).execute(client);
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
                removeResult = new RemoveFileRemoteOperation(path).execute(client);

                assertTrue("Error removing folder " + path + ":" + removeResult, removeResult.isSuccess());
            }
        }
    }
}
