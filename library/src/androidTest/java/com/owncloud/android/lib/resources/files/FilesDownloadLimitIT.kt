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
import com.owncloud.android.lib.resources.status.NextcloudVersion
import com.owncloud.android.lib.resources.status.OCCapability
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class FilesDownloadLimitIT : AbstractIT() {
    @Before
    fun before() {
        testOnlyOnServer(NextcloudVersion.nextcloud_30)
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
        createTestFile()

        val shareTokens = mutableListOf<String>()

        DOWNLOAD_LIMITS.forEach { limit ->
            val share = createTestShare()
            shareTokens.add(share.token!!)

            val resultSet = SetFilesDownloadLimitRemoteOperation(share.token!!, limit).execute(nextcloudClient)
            assert(resultSet.isSuccess)

            shortSleep()

            val resultGet = GetFilesDownloadLimitRemoteOperation(REMOTE_PATH, false).execute(client)
            assert(resultGet.isSuccess)
            assertEquals(shareTokens.size, resultGet.resultData.size)

            val downloadLimit =
                resultGet.resultData.first {
                    it.token == share.token
                }

            assertEquals(limit, downloadLimit.limit)
            assertEquals(0, downloadLimit.count)
        }

        shortSleep()

        for (i in shareTokens.lastIndex downTo 0) {
            val token = shareTokens[i]
            val resultRemove = RemoveFilesDownloadLimitRemoteOperation(token).execute(nextcloudClient)
            assert(resultRemove.isSuccess)

            shortSleep()

            val resultGet = GetFilesDownloadLimitRemoteOperation(REMOTE_PATH, false).execute(client)
            assert(resultGet.isSuccess)
            assertEquals(i, resultGet.resultData.size)
        }
    }

    private fun getCapability(): OCCapability =
        GetCapabilitiesRemoteOperation().execute(nextcloudClient).singleData as OCCapability

    private fun createTestFile(): Boolean {
        val localPath = createFile("test")
        val result =
            UploadFileRemoteOperation(localPath, REMOTE_PATH, "text/plain", RANDOM_MTIME)
                .execute(client)
                .isSuccess
        assert(result)
        return result
    }

    private fun createTestShare(): OCShare {
        val result =
            CreateShareRemoteOperation(
                REMOTE_PATH,
                ShareType.PUBLIC_LINK,
                "",
                false,
                "",
                1
            ).execute(client)

        assertTrue(result.getLogMessage(), result.isSuccess)
        val share = result.resultData.first()
        assert(share.token != null)
        return share
    }

    companion object {
        private const val REMOTE_PATH = "/downloadLimits.txt"
        private val DOWNLOAD_LIMITS = listOf(5, 10)
    }
}
