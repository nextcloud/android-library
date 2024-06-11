/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.directediting;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.TemplateList;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;

import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DirectEditingObtainListOfTemplatesRemoteOperationIT extends AbstractIT {

    @Test
    public void testGetAll() {
        RemoteOperationResult<TemplateList> result = new DirectEditingObtainListOfTemplatesRemoteOperation("text",
                "textdocument")
                .execute(nextcloudClient);
        assertTrue(result.isSuccess());

        TemplateList templateList = (TemplateList) result.getResultData();

        assertEquals("Empty file", Objects.requireNonNull(templateList.getTemplates().get("empty")).getTitle());
        assertEquals("md", Objects.requireNonNull(templateList.getTemplates().get("empty")).getExtension());
    }
}
