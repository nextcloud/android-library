/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 ZetaTom <70907959+zetatom@users.noreply.github.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.files

import com.nextcloud.android.lib.resources.files.GetFilesDownloadLimitRemoteOperation
import com.nextcloud.android.lib.resources.files.RemoveFilesDownloadLimitRemoteOperation
import com.nextcloud.android.lib.resources.files.SetFilesDownloadLimitRemoteOperation
import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.shares.CreateShareRemoteOperation
import com.owncloud.android.lib.resources.shares.OCShare
import com.owncloud.android.lib.resources.shares.ShareType
import com.owncloud.android.lib.resources.status.GetCapabilitiesRemoteOperation
import com.owncloud.android.lib.resources.status.OCCapability
import com.owncloud.android.lib.resources.status.OwnCloudVersion
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test

class FilesDownloadLimitIT : AbstractIT() {
    @Before
    fun before() {
        testOnlyOnServer(OwnCloudVersion.nextcloud_30)
        assert(getCapability().filesDownloadLimit.isTrue)
    }

    @Test
    fun getDefaultLimit() {
        val defaultLimit = getCapability().filesDownloadLimitDefault
        assertEquals(-1, defaultLimit)
    }

    @Test
    @Suppress("Detekt.MagicNumber")
    fun downloadLimit() {
        val share = createTestShare()
        val limit = 5

        val resultSet = SetFilesDownloadLimitRemoteOperation(share.token!!, limit).execute(nextcloudClient)
        assert(resultSet.isSuccess)

        shortSleep()

        val resultGet1 = GetFilesDownloadLimitRemoteOperation(REMOTE_PATH, false).execute(client)
        assert(resultGet1.isSuccess)
        assert(resultGet1.resultData.size == 1)
        assert(resultGet1.resultData.first().token == share.token)
        assert(resultGet1.resultData.first().limit == limit)
        assert(resultGet1.resultData.first().count == 0)

        shortSleep()

        val resultRemove = RemoveFilesDownloadLimitRemoteOperation(share.token!!).execute(nextcloudClient)
        assert(resultRemove.isSuccess)

        shortSleep()

        val resultGet2 = GetFilesDownloadLimitRemoteOperation(REMOTE_PATH, false).execute(client)
        assert(resultGet2.isSuccess)
        assert(resultGet2.resultData.isEmpty())
    }

    private fun getCapability(): OCCapability =
        GetCapabilitiesRemoteOperation().execute(nextcloudClient).singleData as OCCapability

    private fun createTestShare(): OCShare {
        val localPath = createFile("test")

        assert(
            UploadFileRemoteOperation(localPath, REMOTE_PATH, "text/plain", RANDOM_MTIME)
                .execute(client)
                .isSuccess
        )

        val result =
            CreateShareRemoteOperation(
                REMOTE_PATH,
                ShareType.PUBLIC_LINK,
                "",
                false,
                "",
                1
            ).execute(client)

        assert(result.isSuccess)
        val share = result.resultData.first()
        assert(share.token != null)
        return share
    }

    companion object {
        private const val REMOTE_PATH = "/downloadLimits.txt"
    }
}
