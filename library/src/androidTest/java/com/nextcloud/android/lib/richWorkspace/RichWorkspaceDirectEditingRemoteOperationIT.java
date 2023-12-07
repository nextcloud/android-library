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

package com.nextcloud.android.lib.richWorkspace;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.webkit.URLUtil;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class RichWorkspaceDirectEditingRemoteOperationIT extends AbstractIT {

    @Test
    public void getEditLinkForRoot() {
        RemoteOperationResult result = new RichWorkspaceDirectEditingRemoteOperation("/").execute(client);
        assertTrue(result.isSuccess());
        assertNotNull(result.getSingleData());

        String url = (String) result.getSingleData();

        assertTrue(URLUtil.isValidUrl(url));
    }

    @Test
    public void getEditLinkForFolder() {
        String path = "/workspace/sub1/";

        assertTrue(new CreateFolderRemoteOperation(path, true).execute(client).isSuccess());

        RemoteOperationResult result = new RichWorkspaceDirectEditingRemoteOperation(path).execute(client);
        assertTrue(result.isSuccess());
        assertNotNull(result.getSingleData());

        String url = (String) result.getSingleData();

        assertTrue(URLUtil.isValidUrl(url));
    }

    @Test
    public void reuseExistingFile() throws IOException {
        String folder = "/Workspace/";
        String filePath = folder + "Readme.md";
        File txtFile = getFile(ASSETS__TEXT_FILE_NAME);

        assertTrue(new CreateFolderRemoteOperation(folder, true).execute(client).isSuccess());

        RemoteOperationResult uploadResult = new UploadFileRemoteOperation(
                txtFile.getAbsolutePath(),
                filePath,
                "txt/plain",
                System.currentTimeMillis() / MILLI_TO_SECOND
        ).execute(client);

        assertTrue("Error uploading file " + filePath + ": " + uploadResult, uploadResult.isSuccess());

        RemoteOperationResult result = new RichWorkspaceDirectEditingRemoteOperation(folder).execute(client);
        assertTrue(result.isSuccess());
        assertNotNull(result.getSingleData());

        String url = (String) result.getSingleData();

        assertTrue(URLUtil.isValidUrl(url));
    }
}
