/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.directediting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.TemplateList;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;

import org.junit.Test;

public class DirectEditingObtainListOfTemplatesRemoteOperationIT extends AbstractIT {

    @Test
    public void testGetAll() {
        RemoteOperationResult result = new DirectEditingObtainListOfTemplatesRemoteOperation("text",
                "textdocument")
                .execute(client);
        assertTrue(result.isSuccess());

        TemplateList templateList = (TemplateList) result.getResultData();

        assertEquals("Empty file", templateList.getTemplates().get("empty").getTitle());
        assertEquals("md", templateList.getTemplates().get("empty").getExtension());
    }
}
