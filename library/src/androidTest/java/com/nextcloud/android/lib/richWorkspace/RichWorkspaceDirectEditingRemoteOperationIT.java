/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.richWorkspace;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.webkit.URLUtil;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation;
import com.owncloud.android.lib.resources.status.OwnCloudVersion;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class RichWorkspaceDirectEditingRemoteOperationIT extends AbstractIT {
    @BeforeClass
    public static void beforeClass() {
        requireServerVersion(OwnCloudVersion.nextcloud_18);
    }

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
