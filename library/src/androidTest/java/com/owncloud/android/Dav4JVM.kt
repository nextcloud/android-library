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
import com.nextcloud.common.NextcloudAuthenticator
import com.nextcloud.operations.PropFindMethod
import com.nextcloud.test.RandomStringGenerator
import com.owncloud.android.lib.common.network.WebdavUtils
import com.owncloud.android.lib.common.utils.WebDavFileUtils
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperationIT
import com.owncloud.android.lib.resources.files.SearchRemoteOperation
import com.owncloud.android.lib.resources.files.ToggleFavoriteRemoteOperation
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation
import com.owncloud.android.lib.resources.files.model.RemoteFile
import com.owncloud.android.lib.resources.shares.CreateShareRemoteOperation
import com.owncloud.android.lib.resources.shares.OCShare
import com.owncloud.android.lib.resources.shares.ShareType
import com.owncloud.android.lib.resources.status.OCCapability
import com.owncloud.android.lib.resources.tags.CreateTagRemoteOperation
import com.owncloud.android.lib.resources.tags.GetTagsRemoteOperation
import com.owncloud.android.lib.resources.tags.PutTagRemoteOperation
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
        val subFolder = path + "subfolder/"

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

        // add one child
        // create folder
        CreateFolderRemoteOperation(
            subFolder,
            true
        ).execute(client).isSuccess

        // verify folder
        assertTrue(ReadFolderRemoteOperation(subFolder).execute(client).isSuccess)

        // do old read folder operation to compare data against it
        var result = ReadFolderRemoteOperation(path).execute(client).data as List<RemoteFile>
        assertEquals(2, result.size)
        var oldRemoteFile = result[0]
        var oldSubFolderFile = result[1]

        assertEquals(path, oldRemoteFile.remotePath)
        assertEquals(subFolder, oldSubFolderFile.remotePath)

        // create tag
        val tag1 = "a" + RandomStringGenerator.make(ReadFolderRemoteOperationIT.TAG_LENGTH)
        assertTrue(CreateTagRemoteOperation(tag1).execute(nextcloudClient).isSuccess)

        // list tags
        val tags = GetTagsRemoteOperation().execute(client).resultData

        // add tag
        assertTrue(
            PutTagRemoteOperation(
                tags[0].id,
                oldRemoteFile.localId
            ).execute(nextcloudClient).isSuccess
        )

        // do old read folder operation to compare data against it
        result = ReadFolderRemoteOperation(path).execute(client).data as List<RemoteFile>
        assertEquals(2, result.size)
        oldRemoteFile = result[0]
        oldSubFolderFile = result[1]

        // new
        val httpUrl = (nextcloudClient.filesDavUri.toString() + path).toHttpUrl()

        var davResponse: Response? = null

        val memberElements: MutableList<Response> = ArrayList()
        var rootElement: Response? = null

        // disable redirect
        val client = nextcloudClient.client
            .newBuilder()
            .followRedirects(false)
            .authenticator(NextcloudAuthenticator(nextcloudClient.credentials))
            .build()

        // register custom property
        // TODO check how to do it in a central way
        WebdavUtils.registerCustomFactories()

        // TODO use DavResource().propfind in ReadFileRemoteOperation/ReadFolderRemoteOperation
        // TODO test all properties on server!
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
        assertEquals(1, memberElements.size)

        val remoteFile = WebDavFileUtils().parseResponse(rootElement, nextcloudClient.filesDavUri)
        assertTrue(oldRemoteFile == remoteFile)

        val subfolderFile =
            WebDavFileUtils().parseResponse(memberElements[0], nextcloudClient.filesDavUri)
        assertTrue(oldSubFolderFile == subfolderFile)

        // new propfind
        val newResult = nextcloudClient.execute(PropFindMethod(httpUrl))

        assertTrue(newResult.success)
        assertTrue(oldRemoteFile == newResult.root)

        assertEquals(1, newResult.children.size)
        assertTrue(oldSubFolderFile == newResult.children[0])
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
            OCCapability().apply {
                versionMayor = 23
                versionMinor = 0
                versionMicro = 0
            }
        ).execute(
            client
        )

        assertTrue(ror.isSuccess)
        assertEquals(2, ror.resultData.size)

        val oldRemoteFile = ror.resultData[0]

        assertTrue(
            path == ror.resultData[0].remotePath ||
                path == ror.resultData[1].remotePath
        )

        ror = SearchRemoteOperation(
            "test",
            SearchRemoteOperation.SearchType.FILE_SEARCH,
            false,
            OCCapability().apply {
                versionMayor = 23
                versionMinor = 0
                versionMicro = 0
            }
        ).execute(
            nextcloudClient
        )

        assertTrue(ror.isSuccess)
        assertEquals(2, ror.resultData.size)

        val remoteFile = ror.resultData[0]
        assertTrue(
            path == ror.resultData[0].remotePath ||
                path == ror.resultData[1].remotePath
        )

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
