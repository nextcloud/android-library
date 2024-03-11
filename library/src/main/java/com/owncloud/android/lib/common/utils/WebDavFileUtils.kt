/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.utils

import android.net.Uri
import at.bitfire.dav4jvm.Response
import at.bitfire.dav4jvm.property.webdav.DisplayName
import at.bitfire.dav4jvm.property.webdav.GetContentLength
import at.bitfire.dav4jvm.property.webdav.GetContentType
import at.bitfire.dav4jvm.property.webdav.ResourceType
import com.owncloud.android.lib.common.network.WebdavEntry
import com.owncloud.android.lib.resources.files.model.RemoteFile
import com.owncloud.android.lib.resources.files.webdav.NCEtag
import com.owncloud.android.lib.resources.files.webdav.NCFavorite
import com.owncloud.android.lib.resources.files.webdav.NCGetLastModified
import com.owncloud.android.lib.resources.files.webdav.NCMountType
import com.owncloud.android.lib.resources.files.webdav.NCPermissions
import com.owncloud.android.lib.resources.files.webdav.NCRichWorkspace
import com.owncloud.android.lib.resources.files.webdav.NCSharees
import com.owncloud.android.lib.resources.files.webdav.NCTags
import com.owncloud.android.lib.resources.files.webdav.OCId
import com.owncloud.android.lib.resources.files.webdav.OCLocalId
import com.owncloud.android.lib.resources.files.webdav.OCOwnerDisplayName
import com.owncloud.android.lib.resources.files.webdav.OCOwnerId
import com.owncloud.android.lib.resources.files.webdav.OCSize
import org.apache.jackrabbit.webdav.MultiStatus

/**
 * WebDav helper.
 */
object WebDavFileUtils {
    /**
     * Read the data retrieved from the server about the contents of the target folder
     *
     * @param remoteData  Full response got from the server with the data of the target
     * folder and its direct children.
     * @param filesDavUri uri to files webdav uri
     * @return content of the target folder
     */
    fun readData(
        remoteData: MultiStatus,
        filesDavUri: Uri,
        isReadFolderOperation: Boolean,
        isSearchOperation: Boolean
    ): ArrayList<RemoteFile> {
        val mFolderAndFiles = ArrayList<RemoteFile>()
        var we: WebdavEntry
        var start = 1
        if (isReadFolderOperation) {
            we = WebdavEntry(
                remoteData.responses[0],
                filesDavUri.encodedPath!!
            )
            mFolderAndFiles.add(RemoteFile(we))
        } else {
            start = 0
        }

        // loop to update every child
        var remoteFile: RemoteFile
        val responses = remoteData.responses
        for (i in start until responses.size) {
            /// new OCFile instance with the data from the server
            we = WebdavEntry(responses[i], filesDavUri.encodedPath!!)
            remoteFile = RemoteFile(we)
            mFolderAndFiles.add(remoteFile)
        }
        return mFolderAndFiles
    }

    fun readData(responses: List<Response>, filesDavUri: Uri): ArrayList<RemoteFile> {
        val list = ArrayList<RemoteFile>()
        for (response in responses) {
            list.add(parseResponse(response, filesDavUri))
        }
        return list
    }

    fun parseResponse(response: Response, filesDavUri: Uri): RemoteFile {
        val remoteFile = RemoteFile()

        // TODO: refactor
        val path = response.href.toString().split(filesDavUri.encodedPath!!.toRegex(), limit = 2)
            .toTypedArray()[1].replace("//", "/")

        for (property in response.properties) {
            when (property) {
                is DisplayName -> remoteFile.name = property.displayName?.apply { substring(1, length - 1) }
                is NCEtag -> remoteFile.etag = property.etag
                is NCFavorite -> remoteFile.isFavorite = property.favorite
                is NCGetLastModified -> remoteFile.modifiedTimestamp = property.lastModified
                is GetContentLength -> remoteFile.length = property.contentLength
                is GetContentType -> remoteFile.mimeType = (property.type ?: "").toString()
                is ResourceType -> if (property.types.contains(ResourceType.COLLECTION)) {
                    remoteFile.mimeType = WebdavEntry.DIR_TYPE
                }
                is NCPermissions -> remoteFile.permissions = property.permissions
                is OCId -> remoteFile.remoteId = property.id
                is OCSize -> remoteFile.size = property.size
                is OCLocalId -> remoteFile.localId = property.localId
                is NCMountType -> remoteFile.mountType = property.mountType
                is OCOwnerId -> remoteFile.ownerId = property.ownerId
                is OCOwnerDisplayName -> remoteFile.ownerDisplayName = property.ownerDisplayName
                is NCRichWorkspace -> remoteFile.richWorkspace = property.richWorkspace
                is NCSharees -> remoteFile.sharees = property.sharees
                is NCTags -> remoteFile.tags = property.tags
            }
        }

        remoteFile.remotePath = path

        // displayName not set - get from path
        if (remoteFile.name.isNullOrEmpty()) {
            remoteFile.name = path.substringAfterLast("/")
        }

        return remoteFile
    }
}
