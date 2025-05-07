/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019-2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.operations;

import static org.junit.Assert.assertTrue;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation;
import com.owncloud.android.lib.resources.shares.CreateShareRemoteOperation;
import com.owncloud.android.lib.resources.shares.OCShare;
import com.owncloud.android.lib.resources.shares.RemoveShareRemoteOperation;
import com.owncloud.android.lib.resources.shares.ShareType;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RemoveShareIT extends AbstractIT {
    private static final String FILE_TO_UNSHARE = "/fileToUnshare.txt";

    /**
     * Test remove share
     */
    @Test
    public void testRemoveShare() throws IOException {
        File textFile = getFile(ASSETS__TEXT_FILE_NAME);
        assertTrue(new UploadFileRemoteOperation(
                textFile.getAbsolutePath(),
                FILE_TO_UNSHARE,
                "txt/plain",
                System.currentTimeMillis() / MILLI_TO_SECOND
        ).execute(client).isSuccess());

        RemoteOperationResult<List<OCShare>> result = new CreateShareRemoteOperation(FILE_TO_UNSHARE,
                ShareType.PUBLIC_LINK,
                "",
                false,
                "", 1).execute(client);

        assertTrue(result.getLogMessage(), result.isSuccess());

        OCShare ocShare = result.getResultData().get(0);

        assertTrue(new RemoveShareRemoteOperation((int) ocShare.getRemoteId()).execute(client)
                .isSuccess());
    }
}
