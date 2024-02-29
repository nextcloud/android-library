/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.shares

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation
import com.owncloud.android.lib.resources.status.GetStatusRemoteOperation
import com.owncloud.android.lib.resources.status.NextcloudVersion
import com.owncloud.android.lib.resources.status.OwnCloudVersion
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assume
import org.junit.Before
import org.junit.Test

class CreateShareRemoteOperationIT : AbstractIT() {
    @Before
    fun before() {
        val result = GetStatusRemoteOperation(context).execute(client)
        Assert.assertTrue(result.isSuccess)
        val data = result.data as ArrayList<Any>
        val ownCloudVersion = data[0] as OwnCloudVersion
        Assume.assumeTrue(ownCloudVersion.isNewerOrEqual(NextcloudVersion.nextcloud_24))
    }

    @Test
    fun createShareWithNote() {
        val note = "This is the note"

        Assert.assertTrue(
            CreateFolderRemoteOperation(
                "/share/",
                true
            ).execute(client).isSuccess
        )

        // share folder to user "admin"
        val sut =
            CreateShareRemoteOperation(
                "/share/",
                ShareType.USER,
                "admin",
                false,
                "",
                OCShare.MAXIMUM_PERMISSIONS_FOR_FOLDER,
                true,
                note
            ).execute(client)

        junit.framework.Assert.assertTrue(sut.isSuccess)

        val share = sut.resultData[0]

        assertEquals(note, share.note)
    }
}
