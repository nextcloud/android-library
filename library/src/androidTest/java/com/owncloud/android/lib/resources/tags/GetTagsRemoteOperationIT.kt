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
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod
import org.apache.jackrabbit.webdav.client.methods.PropPatchMethod
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet
import org.apache.jackrabbit.webdav.property.DavPropertySet
import org.apache.jackrabbit.webdav.property.DefaultDavProperty
import org.apache.jackrabbit.webdav.xml.Namespace
import org.junit.After
import org.junit.Test

class GetTagsRemoteOperationIT : AbstractIT() {
    companion object {
        const val TAG_LENGTH = 10
        const val TAG_URL = "/remote.php/dav/systemtags/"
    }

    private val createdTags = mutableListOf<Tag>()

    @After
    fun deleteCreatedTags() {
        createdTags.forEach {
            val deleteMethod = DeleteMethod(client2.baseUri.toString() + TAG_URL + it.id)
            client2.executeMethod(deleteMethod)
            deleteMethod.releaseConnection()
        }
        createdTags.clear()
    }

    private fun createTag(): Tag {
        val name = RandomStringGenerator.make(TAG_LENGTH)
        assertTrue(
            CreateTagRemoteOperation(name)
                .execute(nextcloudClient)
                .isSuccess
        )

        val result = GetTagsRemoteOperation().execute(client)
        assertTrue(result.isSuccess)

        val tag = result.resultData.find { it.name == name }
        assertNotNull(tag)

        return tag!!.also { createdTags.add(it) }
    }

    @Test
    @Suppress("LongMethod")
    fun list() {
        testOnlyOnServer(NextcloudVersion.nextcloud_31)

        var sut = GetTagsRemoteOperation().execute(client)
        assertTrue(sut.isSuccess)

        val count = sut.resultData.size

        val tag1 = createTag()
        val tag2 = createTag()

        sut = GetTagsRemoteOperation().execute(client)
        assertTrue(sut.isSuccess)
        assertEquals(count + 2, sut.resultData.size)

        val plainColor = "ff00ff"
        val colorWithHex = "#$plainColor"
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
                client2.baseUri.toString() + TAG_URL + tag1.id,
                newProps,
                DavPropertyNameSet()
            )
        val propPatchStatus = client2.executeMethod(propPatchMethod)
        propPatchMethod.releaseConnection()
        assertEquals(HttpStatus.SC_MULTI_STATUS, propPatchStatus)

        sut = GetTagsRemoteOperation().execute(client)
        assertTrue(sut.isSuccess)
        assertEquals(colorWithHex, sut.resultData.find { it.id == tag1.id }?.color)
        assertEquals(null, sut.resultData.find { it.id == tag2.id }?.color)

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
        tags?.sortBy { it?.color }
        assertEquals(2, tags?.size)
        assertEquals(null, tags?.first()?.color)
        assertEquals(colorWithHex, tags?.last()?.color)
    }
}
