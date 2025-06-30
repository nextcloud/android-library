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
import com.owncloud.android.lib.resources.shares.attributes.ShareAttributes
import com.owncloud.android.lib.resources.shares.attributes.ShareAttributesJsonHandler
import com.owncloud.android.lib.resources.status.GetStatusRemoteOperation
import com.owncloud.android.lib.resources.status.NextcloudVersion
import com.owncloud.android.lib.resources.status.OwnCloudVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Before
import org.junit.Test

class CreateShareRemoteOperationIT : AbstractIT() {
    private var ownCloudVersion: OwnCloudVersion? = null

    @Before
    fun before() {
        val result = GetStatusRemoteOperation(context).execute(client)
        assertTrue(result.isSuccess)
        val data = result.data as ArrayList<Any>
        ownCloudVersion = data[0] as OwnCloudVersion
        Assume.assumeTrue(ownCloudVersion?.isNewerOrEqual(NextcloudVersion.nextcloud_24) == true)
    }

    @Test
    fun createShareWithNoteAndAttributes() {
        val attributes = listOf(
            ShareAttributes.createDownloadAttributes(
                true,
                ownCloudVersion?.isNewerOrEqual(NextcloudVersion.nextcloud_30) == true
            )
        )
        val note = "Note with attributes"
        val path = "/shareWithAttributes/"

        createFolder(path)
        val share = createShare(path, "admin", note, ShareAttributesJsonHandler.toJson(attributes))
        assertEquals(note, share.note)
        assertEquals(attributes, ShareAttributesJsonHandler.toList(share.attributes))
    }

    @Test
    fun createShareWithNote() {
        val note = "This is the note"
        val path = "/share/"

        createFolder(path)
        val share = createShare(path, "admin", note)
        assertEquals(note, share.note)
    }

    private fun createFolder(path: String) {
        assertTrue(CreateFolderRemoteOperation(path, true).execute(client).isSuccess)
    }

    private fun createShare(
        path: String,
        accountName: String,
        note: String,
        attributes: String? = null
    ): OCShare {
        val operation =
            CreateShareRemoteOperation(
                path,
                ShareType.USER,
                accountName,
                false,
                "",
                OCShare.MAXIMUM_PERMISSIONS_FOR_FOLDER,
                true,
                note,
                attributes
            )
        val result = operation.execute(client)
        assertTrue(result.isSuccess)
        return result.resultData[0]
    }
}
