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

package com.owncloud.android

import at.bitfire.dav4jvm.DavResource
import at.bitfire.dav4jvm.Response
import at.bitfire.dav4jvm.property.CreationDate
import com.nextcloud.common.NextcloudAuthenticator
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.utils.WebDavFileUtils
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation
import com.owncloud.android.lib.resources.files.SearchRemoteOperation
import com.owncloud.android.lib.resources.files.ToggleFavoriteRemoteOperation
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation
import com.owncloud.android.lib.resources.files.model.RemoteFile
import com.owncloud.android.lib.resources.files.webdav.NCFavorite
import com.owncloud.android.lib.resources.shares.CreateShareRemoteOperation
import com.owncloud.android.lib.resources.shares.OCShare
import com.owncloud.android.lib.resources.shares.ShareType
import com.owncloud.android.lib.resources.status.OCCapability
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.apache.jackrabbit.webdav.DavConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/*
can be removed after fully switching to dav4jvm as other tests should cover it
 */
class Dav4JVM : AbstractIT() {
    @Test
    @Throws(IOException::class)
    fun singlePropfind() {
        val path = "/testFolder/"

        // create folder
        CreateFolderRemoteOperation(
            path,
            true
        ).execute(client).isSuccess

        // verify folder
        assertTrue(ReadFolderRemoteOperation(path).execute(client).isSuccess)

        // add favorite
        assertTrue(ToggleFavoriteRemoteOperation(true, path).execute(client).isSuccess)

        // share it
        assertTrue(
            CreateShareRemoteOperation(
                path,
                ShareType.USER,
                "admin",
                false,
                "",
                OCShare.MAXIMUM_PERMISSIONS_FOR_FOLDER,
                true
            ).execute(client)
                .isSuccess
        )

        // do old read folder operation to compare data against it
        val result = ReadFolderRemoteOperation(path).execute(client).data as List<RemoteFile>
        val oldRemoteFile = result[0]

        // new
        val httpUrl = (nextcloudClient.filesDavUri.toString() + path).toHttpUrl()

        var davResponse: Response? = null

        val memberElements: MutableList<Response> = ArrayList()
        var rootElement: Response? = null

        // disable redirect
        val client = nextcloudClient.client
            .newBuilder()
            .followRedirects(false)
            .authenticator(NextcloudAuthenticator(nextcloudClient.credentials, "Authorization"))
            .build()

        // register custom property
        WebdavUtils.registerCustomFactories()

        DavResource(client, httpUrl)
            .propfind(
                DavConstants.DEPTH_1,
                *WebdavUtils.getAllPropertiesList()
            ) { response: Response, hrefRelation: Response.HrefRelation? ->
                davResponse = response
                when (hrefRelation) {
                    Response.HrefRelation.MEMBER -> memberElements.add(response)
                    Response.HrefRelation.SELF -> rootElement = response
                    Response.HrefRelation.OTHER -> {}
                    else -> {}
                }
            }

        assertTrue(davResponse?.isSuccess() == true)
        assertTrue(rootElement != null)
        assertEquals(0, memberElements.size)

        val remoteFile = WebDavFileUtils().parseResponse(rootElement, nextcloudClient.filesDavUri)

        val date = davResponse?.get(CreationDate::class.java)
        assertEquals(
            oldRemoteFile.creationTimestamp,
            (WebdavUtils.parseResponseDate(date?.creationDate)?.time ?: 0) / 1000
        )

        assertTrue(oldRemoteFile.isFavorite)
        val favorite = davResponse?.get(NCFavorite::class.java)
        assertTrue(favorite?.isOcFavorite == true)

        assertEquals(oldRemoteFile.remotePath, remoteFile.remotePath)
        assertEquals(oldRemoteFile.mimeType, remoteFile.mimeType)
        assertEquals(oldRemoteFile.length, remoteFile.length)
        assertEquals(oldRemoteFile.creationTimestamp, remoteFile.creationTimestamp)
        // assertEquals(oldRemoteFile.modifiedTimestamp, remoteFile.modifiedTimestamp)
        assertEquals(oldRemoteFile.uploadTimestamp, remoteFile.uploadTimestamp)
        assertEquals(oldRemoteFile.etag, remoteFile.etag)
        assertEquals(oldRemoteFile.permissions, remoteFile.permissions)
        assertEquals(oldRemoteFile.remoteId, remoteFile.remoteId)
        assertEquals(oldRemoteFile.size, remoteFile.size)
        assertEquals(oldRemoteFile.isFavorite, remoteFile.isFavorite)
        assertEquals(oldRemoteFile.isEncrypted, remoteFile.isEncrypted)
        assertEquals(oldRemoteFile.mountType, remoteFile.mountType)
        assertEquals(oldRemoteFile.ownerId, remoteFile.ownerId)
        assertEquals(oldRemoteFile.ownerDisplayName, remoteFile.ownerDisplayName)
        assertEquals(oldRemoteFile.unreadCommentsCount, remoteFile.unreadCommentsCount)
        assertEquals(oldRemoteFile.isHasPreview, remoteFile.isHasPreview)
        assertEquals(oldRemoteFile.note, remoteFile.note)
        assertEquals(oldRemoteFile.sharees.size, remoteFile.sharees.size)
        assertEquals(oldRemoteFile.richWorkspace, remoteFile.richWorkspace)
        assertEquals(oldRemoteFile.isLocked, remoteFile.isLocked)
        assertEquals(oldRemoteFile.lockType, remoteFile.lockType)
        assertEquals(oldRemoteFile.lockOwner, remoteFile.lockOwner)
        assertEquals(oldRemoteFile.lockOwnerDisplayName, remoteFile.lockOwnerDisplayName)
        assertEquals(oldRemoteFile.lockTimestamp, remoteFile.lockTimestamp)
        assertEquals(oldRemoteFile.lockOwnerEditor, remoteFile.lockOwnerEditor)
        assertEquals(oldRemoteFile.lockTimeout, remoteFile.lockTimeout)
        assertEquals(oldRemoteFile.lockToken, remoteFile.lockToken)
        assertEquals(oldRemoteFile.localId, remoteFile.localId)
    }

    @Test
    fun search() {
        val path = "/testFolder/"

        // create folder
        assertTrue(
            CreateFolderRemoteOperation(
                path,
                true
            ).execute(client).isSuccess
        )

        // create file
        val filePath = createFile("text")
        val remotePath = "/test.md"

        assertTrue(
            UploadFileRemoteOperation(
                filePath,
                remotePath,
                "text/markdown",
                "",
                RANDOM_MTIME,
                System.currentTimeMillis(),
                true
            ).execute(client).isSuccess
        )

        WebdavUtils.registerCustomFactories()

        var ror = SearchRemoteOperation(
            "test",
            SearchRemoteOperation.SearchType.FILE_SEARCH,
            false,
            OCCapability(23, 0, 0)
        ).execute(
            client
        )

        assertTrue(ror.isSuccess)
        assertEquals(2, ror.resultData.size)

        val oldRemoteFile = ror.resultData[0]
        assertEquals(path, oldRemoteFile.remotePath)

        ror = SearchRemoteOperation(
            "test",
            SearchRemoteOperation.SearchType.FILE_SEARCH,
            false,
            OCCapability(23, 0, 0)
        ).execute(
            nextcloudClient
        )

        assertTrue(ror.isSuccess)
        assertEquals(2, ror.resultData.size)

        val remoteFile = ror.resultData[0]
        assertEquals(path, remoteFile.remotePath)

        assertEquals(oldRemoteFile.remoteId, remoteFile.remoteId)
    }

    @Test
    fun propPatch() {
        val path = "/testFolder/"

        // create folder
        assertTrue(CreateFolderRemoteOperation(path, true).execute(client).isSuccess)

        // make it favorite
        assertTrue(
            ToggleFavoriteRemoteOperation(true, path).execute(nextcloudClient).isSuccess
        )

        val result = ReadFolderRemoteOperation(path).execute(client)
        assertTrue(result.isSuccess)
        val list = result.data as List<RemoteFile>
        assertTrue(list[0].isFavorite)
    }
}
