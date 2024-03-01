/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.directediting;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.ReadFileRemoteOperation;
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation;

import junit.framework.TestCase;

import org.junit.Test;

import java.io.IOException;

public class DirectEditingOpenFileRemoteOperationIT extends AbstractIT {
    @Test
    public void openFile() throws IOException {
        // create file
        String filePath = createFile("text");
        String remotePath = "/text.md";
        TestCase.assertTrue(new UploadFileRemoteOperation(
                filePath,
                remotePath,
                "text/markdown",
                1464818400
        ).execute(client).isSuccess());

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
        TestCase.assertTrue(new UploadFileRemoteOperation(
                filePath,
                remotePath,
                "text/markdown",
                1464818400
        ).execute(client).isSuccess());

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
        TestCase.assertTrue(new UploadFileRemoteOperation(
                filePath,
                remotePath,
                "text/markdown",
                1464818400
        ).execute(client).isSuccess());

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
