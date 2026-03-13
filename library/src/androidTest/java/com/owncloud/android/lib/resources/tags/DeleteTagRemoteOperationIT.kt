/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.tags

import com.nextcloud.test.RandomStringGenerator
import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation
import com.owncloud.android.lib.resources.files.ReadFileRemoteOperation
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation
import com.owncloud.android.lib.resources.files.model.RemoteFile
import com.owncloud.android.lib.resources.status.NextcloudVersion
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class DeleteTagRemoteOperationIT : AbstractIT() {
    companion object {
        const val TAG_LENGTH = 10
    }

    @Test
    fun deleteTag() {
        testOnlyOnServer(NextcloudVersion.nextcloud_31)

        // create a folder
        val folder = "/deleteTagFolder/"
        assertTrue(CreateFolderRemoteOperation(folder, true).execute(client).isSuccess)
        val folderMetadata = ReadFileRemoteOperation(folder).execute(client)
        val fileId = (folderMetadata.data[0] as RemoteFile).localId

        // create a tag
        val tagName = RandomStringGenerator.make(TAG_LENGTH)
        assertTrue(
            CreateTagRemoteOperation(tagName)
                .execute(nextcloudClient)
                .isSuccess
        )

        // find the created tag
        val tagsResult = GetTagsRemoteOperation().execute(client)
        assertTrue(tagsResult.isSuccess)
        val tag = tagsResult.resultData.find { it.name == tagName }
        assertTrue(tag != null)

        // assign the tag to the folder
        assertTrue(
            PutTagRemoteOperation(tag!!.id, fileId)
                .execute(nextcloudClient)
                .isSuccess
        )

        // verify the tag is on the folder
        var rootMetadata = ReadFolderRemoteOperation("/").execute(client)
        var folderTags =
            (rootMetadata.data as ArrayList<RemoteFile>)
                .find { it.remotePath == folder }
                ?.tags
        assertEquals(1, folderTags?.size)
        assertEquals(tagName, folderTags?.first()?.name)

        // delete the tag from the folder
        assertTrue(
            DeleteTagRemoteOperation(tag.id, fileId)
                .execute(nextcloudClient)
                .isSuccess
        )

        // verify the tag is no longer on the folder
        rootMetadata = ReadFolderRemoteOperation("/").execute(client)
        folderTags =
            (rootMetadata.data as ArrayList<RemoteFile>)
                .find { it.remotePath == folder }
                ?.tags
        assertTrue(folderTags.isNullOrEmpty())
    }
}
