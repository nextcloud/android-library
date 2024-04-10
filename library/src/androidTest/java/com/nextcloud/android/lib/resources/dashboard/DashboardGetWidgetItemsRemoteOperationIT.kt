/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.dashboard

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation
import com.owncloud.android.lib.resources.files.RemoveFileRemoteOperation
import com.owncloud.android.lib.resources.shares.CreateShareRemoteOperation
import com.owncloud.android.lib.resources.shares.OCShare
import com.owncloud.android.lib.resources.shares.ShareType
import com.owncloud.android.lib.resources.status.NextcloudVersion
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardGetWidgetItemsRemoteOperationIT : AbstractIT() {
    @Test
    fun getItems() {
        // only on NC25+
        testOnlyOnServer(NextcloudVersion.nextcloud_25)

        val folderPath = "/testFolder"

        // create folder to have some content
        assertTrue(CreateFolderRemoteOperation(folderPath, false).execute(nextcloudClient2).isSuccess)
        assertTrue(
            CreateShareRemoteOperation(
                folderPath,
                ShareType.USER,
                client.userId,
                false,
                "",
                OCShare.MAXIMUM_PERMISSIONS_FOR_FOLDER
            ).execute(client2)
                .isSuccess
        )

        val widgetId = "activity"
        val result = DashboardGetWidgetItemsRemoteOperation(widgetId, LIMIT_SIZE).execute(nextcloudClient)

        assertTrue(result.isSuccess)
        assertTrue(result.resultData?.get(widgetId)?.isNotEmpty() ?: false)

        val firstResult = result.resultData?.get(widgetId)?.get(0)
        assertTrue(firstResult?.title?.isNotEmpty() == true)
        assertTrue(firstResult?.subtitle != null)
        assertTrue(firstResult?.link?.isNotEmpty() == true)
        assertTrue(firstResult?.iconUrl?.isNotEmpty() == true)

        // remove folder
        assertTrue(RemoveFileRemoteOperation(folderPath).execute(nextcloudClient2).isSuccess)
    }

    @Test
    fun getEmptyItems() {
        val widgetId = "nonExistingWidget"
        val result =
            DashboardGetWidgetItemsRemoteOperation(widgetId, LIMIT_SIZE).execute(nextcloudClient)

        assertTrue(result.isSuccess)
        assertTrue(result.resultData?.isEmpty() ?: false)
    }

    companion object {
        const val LIMIT_SIZE = 14
    }
}
