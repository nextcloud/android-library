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

package com.nextcloud.android.lib.resources.directediting;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.ReadFileRemoteOperation;
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation;

import junit.framework.TestCase;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DirectEditingOpenFileRemoteOperationTest extends AbstractIT {
    @Test
    public void openFile() throws IOException {
        // create file
        String filePath = createFile("text");
        String remotePath = "/text.md";
        TestCase.assertTrue(new UploadFileRemoteOperation(filePath, remotePath, "text/markdown", "123")
                                    .execute(client).isSuccess());

        TestCase.assertTrue(new ReadFileRemoteOperation(remotePath).execute(client).isSuccess());

        // open file
        RemoteOperationResult result = new DirectEditingOpenFileRemoteOperation(remotePath, "text").execute(client);
        assertTrue(result.isSuccess());

        String url = (String) result.getSingleData();

        assertFalse(url.isEmpty());
    }

    @Test
    public void openFileWithSpecialChars() throws IOException {
        // create file
        String filePath = createFile("text");
        String remotePath = "/äää.md";
        TestCase.assertTrue(new UploadFileRemoteOperation(filePath, remotePath, "text/markdown", "123")
                .execute(client).isSuccess());

        TestCase.assertTrue(new ReadFileRemoteOperation(remotePath).execute(client).isSuccess());

        // open file
        RemoteOperationResult result = new DirectEditingOpenFileRemoteOperation(remotePath, "text").execute(client);
        assertTrue(result.isSuccess());

        String url = (String) result.getSingleData();

        assertFalse(url.isEmpty());
    }

    @Test
    public void openFileWithSpecialChars2() throws IOException {
        // create file
        String filePath = createFile("text");
        String remotePath = "/あ.md";
        TestCase.assertTrue(new UploadFileRemoteOperation(filePath, remotePath, "text/markdown", "123")
                .execute(client).isSuccess());

        TestCase.assertTrue(new ReadFileRemoteOperation(remotePath).execute(client).isSuccess());

        // open file
        RemoteOperationResult result = new DirectEditingOpenFileRemoteOperation(remotePath, "text").execute(client);
        assertTrue(result.isSuccess());

        String url = (String) result.getSingleData();

        assertFalse(url.isEmpty());
    }

    @Test
    public void openNonExistingFile() {
        String remotePath = "/nonExisting.md";

        TestCase.assertFalse(new ReadFileRemoteOperation(remotePath).execute(client).isSuccess());

        // open file
        RemoteOperationResult result = new DirectEditingOpenFileRemoteOperation(remotePath, "text").execute(client);
        assertFalse(result.isSuccess());
    }
}
