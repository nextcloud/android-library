/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import com.nextcloud.test.RandomStringGenerator
import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.common.network.WebdavEntry
import com.owncloud.android.lib.resources.status.NextcloudVersion
import com.owncloud.android.lib.resources.tags.CreateTagRemoteOperation
import com.owncloud.android.lib.resources.tags.GetTagsRemoteOperation
import com.owncloud.android.lib.resources.tags.GetTagsRemoteOperationIT.Companion.TAG_URL
import com.owncloud.android.lib.resources.tags.PutTagRemoteOperation
import junit.framework.TestCase
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.client.methods.PropPatchMethod
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet
import org.apache.jackrabbit.webdav.property.DavPropertySet
import org.apache.jackrabbit.webdav.property.DefaultDavProperty
import org.apache.jackrabbit.webdav.xml.Namespace
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadFolderRemoteOperationIT : AbstractIT() {
    companion object {
        const val TAG_LENGTH = 10
    }

    @Test
    @Suppress("LongMethod")
    fun readRemoteFolderWithContent() {
        val remotePath = "/test/"

        assertTrue(CreateFolderRemoteOperation(remotePath, true).execute(nextcloudClient).isSuccess)

        // create file
        val filePath = createFile("text")
        assertTrue(
            UploadFileRemoteOperation(filePath, remotePath + "1.txt", "text/markdown", RANDOM_MTIME)
                .execute(client)
                .isSuccess
        )

        var result = ReadFolderRemoteOperation(remotePath).execute(nextcloudClient)

        assertTrue(result.isSuccess)
        assertEquals(2, result.resultData.size)

        // tag testing only on NC27+
        testOnlyOnServer(NextcloudVersion.nextcloud_27)

        // Folder
        var remoteFolder = result.resultData[0]
        assertEquals(remotePath, remoteFolder.remotePath)
        assertEquals(0, remoteFolder.tags.size)

        // File
        var remoteFile = result.resultData[1]
        assertEquals(remotePath + "1.txt", remoteFile.remotePath)
        assertEquals(0, remoteFile.tags.size)

        // create tag
        val name1 = "a" + RandomStringGenerator.make(TAG_LENGTH)
        val color1 = "#001122"

        val name2 = "b" + RandomStringGenerator.make(TAG_LENGTH)

        assertTrue(CreateTagRemoteOperation(name1).execute(nextcloudClient).isSuccess)
        assertTrue(CreateTagRemoteOperation(name2).execute(nextcloudClient).isSuccess)

        // list tags
        val tags = GetTagsRemoteOperation().execute(client).resultData
        val tag1 = tags.find { it.name == name1 }
        val tag2 = tags.find { it.name == name2 }

        // add color
        val newProps = DavPropertySet()
        newProps.add(
            DefaultDavProperty(
                "nc:color",
                color1.replace("#", ""),
                Namespace.getNamespace(WebdavEntry.NAMESPACE_NC)
            )
        )
        val propPatchMethod =
            PropPatchMethod(
                client2.baseUri.toString() + TAG_URL + tag1?.id,
                newProps,
                DavPropertyNameSet()
            )
        val propPatchStatus = client2.executeMethod(propPatchMethod)
        propPatchMethod.releaseConnection()
        TestCase.assertEquals(HttpStatus.SC_MULTI_STATUS, propPatchStatus)

        // add tag
        assertTrue(
            PutTagRemoteOperation(
                tag1?.id.orEmpty(),
                remoteFile.localId
            ).execute(nextcloudClient).isSuccess
        )
        assertTrue(
            PutTagRemoteOperation(
                tag2?.id.orEmpty(),
                remoteFile.localId
            ).execute(nextcloudClient).isSuccess
        )

        // check again
        result = ReadFolderRemoteOperation(remotePath).execute(nextcloudClient)

        assertTrue(result.isSuccess)
        assertEquals(2, result.resultData.size)

        // Folder
        remoteFolder = result.resultData[0]
        assertEquals(remotePath, remoteFolder.remotePath)
        assertEquals(0, remoteFolder.tags.size)

        // File
        remoteFile = result.resultData[1]
        assertEquals(remotePath + "1.txt", remoteFile.remotePath)
        assertEquals(2, remoteFile.tags.size)

        remoteFile.tags.sortBy { it?.name }

        // tag1
        val resultTag1 = remoteFile.tags[0]
        assertEquals(name1, resultTag1?.name)
        assertEquals(color1, resultTag1?.color)

        // tag2
        val resultTag2 = remoteFile.tags[1]
        assertEquals(name2, resultTag2?.name)
    }
}
