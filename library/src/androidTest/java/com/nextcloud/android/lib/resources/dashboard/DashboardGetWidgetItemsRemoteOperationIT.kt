/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2022 Tobias Kaminsky
 *   Copyright (C) 2022 Nextcloud GmbH
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

package com.nextcloud.android.lib.resources.dashboard

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation
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

        // create folder to have some content
        assertTrue(CreateFolderRemoteOperation("/testFolder", false).execute(client2).isSuccess)
        assertTrue(
            CreateShareRemoteOperation(
                "/testFolder",
                ShareType.USER,
                client.userId,
                false,
                "",
                OCShare.MAXIMUM_PERMISSIONS_FOR_FOLDER
            ).execute(client2)
                .isSuccess
        )

        val widgetId = "activity"
        val result =
            DashboardGetWidgetItemsRemoteOperation(widgetId, LIMIT_SIZE).execute(nextcloudClient)

        assertTrue(result.isSuccess)
        assertTrue(result.resultData[widgetId]?.isNotEmpty() ?: false)

        val firstResult = result.resultData[widgetId]?.get(0)
        assertTrue(firstResult?.title?.isNotEmpty() == true)
        assertTrue(firstResult?.subtitle != null)
        assertTrue(firstResult?.link?.isNotEmpty() == true)
        assertTrue(firstResult?.iconUrl?.isNotEmpty() == true)
    }

    @Test
    fun getEmptyItems() {
        val widgetId = "nonExistingWidget"
        val result =
            DashboardGetWidgetItemsRemoteOperation(widgetId, LIMIT_SIZE).execute(nextcloudClient)

        assertTrue(result.isSuccess)
        assertTrue(result.resultData.isEmpty())
    }

    companion object {
        const val LIMIT_SIZE = 14
    }
}
