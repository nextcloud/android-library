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
import com.owncloud.android.lib.resources.files.webdav.NCCreationTime
import com.owncloud.android.lib.resources.files.webdav.NCEncrypted
import com.owncloud.android.lib.resources.files.webdav.NCEtag
import com.owncloud.android.lib.resources.files.webdav.NCFavorite
import com.owncloud.android.lib.resources.files.webdav.NCGetLastModified
import com.owncloud.android.lib.resources.files.webdav.NCHidden
import com.owncloud.android.lib.resources.files.webdav.NCLock
import com.owncloud.android.lib.resources.files.webdav.NCLockOwner
import com.owncloud.android.lib.resources.files.webdav.NCLockOwnerDisplayName
import com.owncloud.android.lib.resources.files.webdav.NCLockOwnerEditor
import com.owncloud.android.lib.resources.files.webdav.NCLockOwnerType
import com.owncloud.android.lib.resources.files.webdav.NCLockTime
import com.owncloud.android.lib.resources.files.webdav.NCLockTimeout
import com.owncloud.android.lib.resources.files.webdav.NCLockToken
import com.owncloud.android.lib.resources.files.webdav.NCMetadataGPS
import com.owncloud.android.lib.resources.files.webdav.NCMetadataLivePhoto
import com.owncloud.android.lib.resources.files.webdav.NCMetadataPhotosGPS
import com.owncloud.android.lib.resources.files.webdav.NCMetadataPhotosSize
import com.owncloud.android.lib.resources.files.webdav.NCMetadataSize
import com.owncloud.android.lib.resources.files.webdav.NCMountType
import com.owncloud.android.lib.resources.files.webdav.NCNote
import com.owncloud.android.lib.resources.files.webdav.NCPermissions
import com.owncloud.android.lib.resources.files.webdav.NCPreview
import com.owncloud.android.lib.resources.files.webdav.NCRichWorkspace
import com.owncloud.android.lib.resources.files.webdav.NCSharees
import com.owncloud.android.lib.resources.files.webdav.NCTags
import com.owncloud.android.lib.resources.files.webdav.NCTrashbinDeletionTime
import com.owncloud.android.lib.resources.files.webdav.NCTrashbinFilename
import com.owncloud.android.lib.resources.files.webdav.NCTrashbinLocation
import com.owncloud.android.lib.resources.files.webdav.NCUploadTime
import com.owncloud.android.lib.resources.files.webdav.OCCommentsUnread
import com.owncloud.android.lib.resources.files.webdav.OCDisplayName
import com.owncloud.android.lib.resources.files.webdav.OCId
import com.owncloud.android.lib.resources.files.webdav.OCLocalId
import com.owncloud.android.lib.resources.files.webdav.OCOwnerDisplayName
import com.owncloud.android.lib.resources.files.webdav.OCOwnerId
import com.owncloud.android.lib.resources.files.webdav.OCSize
import org.apache.jackrabbit.webdav.MultiStatus
import java.net.URLDecoder

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
            we =
                WebdavEntry(
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
            // / new OCFile instance with the data from the server
            we = WebdavEntry(responses[i], filesDavUri.encodedPath!!)
            remoteFile = RemoteFile(we)
            mFolderAndFiles.add(remoteFile)
        }
        return mFolderAndFiles
    }

    fun readData(
        responses: List<Response>,
        filesDavUri: Uri
    ): ArrayList<RemoteFile> {
        val list = ArrayList<RemoteFile>()
        for (response in responses) {
            list.add(parseResponse(response, filesDavUri))
        }
        return list
    }

    fun parseResponse(
        response: Response,
        filesDavUri: Uri
    ): RemoteFile {
        val remoteFile = RemoteFile()

        val path = URLDecoder.decode(response.href.toString().substringAfter(filesDavUri.toString()), "UTF-8")

        for (property in response.properties) {
            when (property) {
                is DisplayName -> remoteFile.name = property.displayName ?: ""
                is GetContentLength -> remoteFile.length = property.contentLength
                is GetContentType -> remoteFile.mimeType = (property.type ?: "").toString()
                is ResourceType ->
                    if (property.types.contains(ResourceType.COLLECTION)) {
                        remoteFile.mimeType = WebdavEntry.DIR_TYPE
                    }

                is NCCreationTime -> remoteFile.creationTimestamp = property.creationTime
                is NCEncrypted -> remoteFile.isEncrypted = property.encrypted
                is NCEtag -> remoteFile.etag = property.etag
                is NCFavorite -> remoteFile.isFavorite = property.favorite
                is NCGetLastModified -> remoteFile.modifiedTimestamp = property.lastModified
                is NCHidden -> remoteFile.hidden = property.hidden
                is NCLock -> remoteFile.isLocked = property.locked
                is NCLockOwner -> remoteFile.lockOwner = property.lockOwner
                is NCLockOwnerDisplayName -> remoteFile.lockOwnerDisplayName = property.lockOwnerDisplayName
                is NCLockOwnerEditor -> remoteFile.lockOwnerEditor = property.lockOwnerEditor
                is NCLockOwnerType -> remoteFile.lockType = property.lockOwnerType
                is NCLockTime -> remoteFile.lockTimestamp = property.lockTime
                is NCLockTimeout -> remoteFile.lockTimeout = property.lockTimeout
                is NCLockToken -> remoteFile.lockToken = property.lockToken
                is NCMetadataGPS -> remoteFile.geoLocation = property.geoLocation
                is NCMetadataLivePhoto -> remoteFile.livePhoto = property.livePhoto
                is NCMetadataPhotosGPS -> remoteFile.geoLocation = property.geoLocation
                is NCMetadataPhotosSize -> remoteFile.imageDimension = property.imageDimension
                is NCMetadataSize -> remoteFile.imageDimension = property.imageDimension
                is NCMountType -> remoteFile.mountType = property.mountType
                is NCNote -> remoteFile.note = property.note
                is NCPermissions -> remoteFile.permissions = property.permissions
                is NCPreview -> remoteFile.isHasPreview = property.preview
                is NCRichWorkspace -> remoteFile.richWorkspace = property.richWorkspace
                is NCSharees -> remoteFile.sharees = property.sharees
                is NCTags -> remoteFile.tags = property.tags
                is NCTrashbinDeletionTime -> { /* TODO */ }
                is NCTrashbinFilename -> { /* TODO */ }
                is NCTrashbinLocation -> { /* TODO */ }
                is NCUploadTime -> remoteFile.uploadTimestamp = property.uploadTime
                is OCCommentsUnread -> remoteFile.unreadCommentsCount = property.commentsCount
                is OCDisplayName -> remoteFile.name = property.displayName
                is OCId -> remoteFile.remoteId = property.id
                is OCLocalId -> remoteFile.localId = property.localId
                is OCOwnerDisplayName -> remoteFile.ownerDisplayName = property.ownerDisplayName ?: ""
                is OCOwnerId -> remoteFile.ownerId = property.ownerId ?: ""
                is OCSize -> remoteFile.size = property.size
            }
        }

        remoteFile.remotePath = path

        // displayName not set - get from path
        if (remoteFile.name.isEmpty()) {
            remoteFile.name = path.substringAfterLast("/")
        }

        return remoteFile
    }
}
