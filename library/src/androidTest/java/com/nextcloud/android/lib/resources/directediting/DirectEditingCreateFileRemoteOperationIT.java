/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.directediting;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DirectEditingCreateFileRemoteOperationIT extends AbstractIT {
    @Test
    public void createEmptyFile() {
        RemoteOperationResult<String> result = new DirectEditingCreateFileRemoteOperation("/test.md",
                "text",
                "textdocument")
                .execute(nextcloudClient);
        assertTrue(result.isSuccess());

        String url = result.getResultData();

        assertFalse(url.isEmpty());
    }

    @Test
    public void createFileFromTemplate() {
        RemoteOperationResult<String> result = new DirectEditingCreateFileRemoteOperation("/test.md",
                                                                                          "text",
                                                                                          "textdocument",
                                                                                          "1")
                .execute(nextcloudClient);
        assertTrue(result.isSuccess());

        String url = result.getResultData();

        assertFalse(url.isEmpty());
    }

    @Test
    public void createFileWithSpecialCharacterFromTemplate() {
        RemoteOperationResult<String> result = new DirectEditingCreateFileRemoteOperation("/あ.md",
                                                                                          "text",
                                                                                          "textdocument",
                                                                                          "1")
                .execute(nextcloudClient);
        assertTrue(result.isSuccess());

        String url = result.getResultData();

        assertFalse(url.isEmpty());
    }
}
