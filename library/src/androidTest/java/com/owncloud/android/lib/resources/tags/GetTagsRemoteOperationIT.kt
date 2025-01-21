/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.tags

import com.nextcloud.test.RandomStringGenerator
import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.common.network.WebdavEntry
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation
import com.owncloud.android.lib.resources.files.ReadFileRemoteOperation
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation
import com.owncloud.android.lib.resources.files.model.RemoteFile
import com.owncloud.android.lib.resources.status.NextcloudVersion
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.client.methods.PropPatchMethod
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet
import org.apache.jackrabbit.webdav.property.DavPropertySet
import org.apache.jackrabbit.webdav.property.DefaultDavProperty
import org.apache.jackrabbit.webdav.xml.Namespace
import org.junit.Test

class GetTagsRemoteOperationIT : AbstractIT() {
    companion object {
        const val TAG_LENGTH = 10
    }

    @Test
    @Suppress("LongMethod")
    fun list() {
        testOnlyOnServer(NextcloudVersion.nextcloud_31)

        var sut = GetTagsRemoteOperation().execute(client)
        assertTrue(sut.isSuccess)

        val count = sut.resultData.size

        assertTrue(
            CreateTagRemoteOperation(RandomStringGenerator.make(TAG_LENGTH))
                .execute(nextcloudClient)
                .isSuccess
        )

        assertTrue(
            CreateTagRemoteOperation(RandomStringGenerator.make(TAG_LENGTH))
                .execute(nextcloudClient)
                .isSuccess
        )

        sut = GetTagsRemoteOperation().execute(client)
        assertTrue(sut.isSuccess)
        assertEquals(count + 2, sut.resultData.size)

        // add color to one tag
        val plainColor = "ff00ff"
        val colorWithHex = "#$plainColor"
        val tag1 = sut.resultData.first()
        val tag2 = sut.resultData[1]
        val newProps = DavPropertySet()
        newProps.add(
            DefaultDavProperty(
                "nc:color",
                plainColor,
                Namespace.getNamespace(WebdavEntry.NAMESPACE_NC)
            )
        )
        val propPatchMethod =
            PropPatchMethod(
                client.baseUri.toString() + "/remote.php/dav/systemtags/" + tag1.id,
                newProps,
                DavPropertyNameSet()
            )
        assertTrue(client.executeMethod(propPatchMethod) == HttpStatus.SC_MULTI_STATUS)

        sut = GetTagsRemoteOperation().execute(client)

        assertEquals(colorWithHex, sut.resultData.find { it.id == tag1.id }?.color)

        // add colored tag to file
        val tagFolder = "/coloredFolder/"
        assertTrue(CreateFolderRemoteOperation(tagFolder, true).execute(client).isSuccess)
        val folderMetadata = ReadFileRemoteOperation(tagFolder).execute(client)
        assertTrue(
            PutTagRemoteOperation(
                tag1.id,
                (folderMetadata.data[0] as RemoteFile).localId
            ).execute(nextcloudClient).isSuccess
        )
        assertTrue(
            PutTagRemoteOperation(
                tag2.id,
                (folderMetadata.data[0] as RemoteFile).localId
            ).execute(nextcloudClient).isSuccess
        )

        // read metadata
        val rootMetadata = ReadFolderRemoteOperation("/").execute(client)
        val tags =
            (rootMetadata.data as ArrayList<RemoteFile>)
                .find { it.remotePath == tagFolder }
                ?.tags
        assertEquals(2, tags?.size)
        assertEquals(colorWithHex, tags?.first()?.color)
        assertEquals(null, tags?.get(1)?.color)
    }
}
