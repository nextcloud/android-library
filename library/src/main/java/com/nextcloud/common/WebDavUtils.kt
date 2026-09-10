/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Tobias Kaminsky <tobias.kaminsky@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.common

import android.net.Uri
import at.bitfire.dav4jvm.PropertyRegistry
import at.bitfire.dav4jvm.Response
import at.bitfire.dav4jvm.property.DisplayName
import at.bitfire.dav4jvm.property.GetContentLength
import at.bitfire.dav4jvm.property.GetContentType
import at.bitfire.dav4jvm.property.GetETag
import at.bitfire.dav4jvm.property.ResourceType
import com.google.gson.Gson
import com.owncloud.android.lib.common.network.WebdavEntry
import com.owncloud.android.lib.resources.files.model.RemoteFile
import com.owncloud.android.lib.resources.files.webdav.NCCreationDate
import com.owncloud.android.lib.resources.files.webdav.NCEncrypted
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
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Locale

object WebDavUtils {
    const val NAMESPACE_OC = "http://owncloud.org/ns"
    const val NAMESPACE_NC = "http://nextcloud.org/ns"

    internal val gson = Gson()

    private val DATETIME_FORMATS =
        arrayOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
            SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
            SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US),
            SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
            SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US),
            SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US)
        )

    object PROPERTYSETS {
        val ALL =
            arrayOf(
                DisplayName.NAME,
                GetContentType.NAME,
                ResourceType.NAME,
                GetContentLength.NAME,
                NCGetLastModified.NAME,
                NCCreationDate.NAME,
                GetETag.NAME,
                NCPermissions.NAME,
                OCLocalId.NAME,
                OCId.NAME,
                OCSize.NAME,
                NCFavorite.NAME,
                NCEncrypted.NAME,
                NCMountType.NAME,
                OCOwnerId.NAME,
                OCOwnerDisplayName.NAME,
                OCCommentsUnread.NAME,
                NCPreview.NAME,
                NCNote.NAME,
                NCSharees.NAME,
                NCRichWorkspace.NAME,
                NCCreationDate.NAME,
                NCUploadTime.NAME,
                NCLock.NAME,
                NCLockOwnerType.NAME,
                NCLockOwner.NAME,
                NCLockOwnerDisplayName.NAME,
                NCLockOwnerEditor.NAME,
                NCLockTime.NAME,
                NCLockTimeout.NAME,
                NCLockToken.NAME,
                NCTags.NAME,
                NCMetadataSize.NAME,
                NCMetadataGPS.NAME,
                NCMetadataSize.NAME,
                NCMetadataPhotosSize.NAME,
                NCMetadataPhotosGPS.NAME,
                NCMetadataLivePhoto.NAME,
                NCHidden.NAME
            )

        val FILE =
            arrayOf(
                DisplayName.NAME,
                GetContentType.NAME,
                ResourceType.NAME,
                GetContentLength.NAME,
                NCGetLastModified.NAME,
                NCCreationDate.NAME,
                GetETag.NAME,
                NCPermissions.NAME,
                OCLocalId.NAME,
                OCId.NAME,
                OCSize.NAME,
                NCFavorite.NAME,
                NCPreview.NAME,
                NCSharees.NAME,
                NCUploadTime.NAME,
                NCLock.NAME,
                NCLockOwnerType.NAME,
                NCLockOwner.NAME,
                NCLockOwnerDisplayName.NAME,
                NCLockOwnerEditor.NAME,
                NCLockTime.NAME,
                NCLockTimeout.NAME,
                NCLockToken.NAME,
                NCEncrypted.NAME,
                NCTags.NAME,
                NCMetadataSize.NAME,
                NCMetadataGPS.NAME,
                NCMetadataPhotosSize.NAME,
                NCMetadataPhotosGPS.NAME,
                NCMetadataLivePhoto.NAME,
                NCHidden.NAME
            )

        val TRASHBIN =
            arrayOf(
                ResourceType.NAME,
                GetContentType.NAME,
                GetContentLength.NAME,
                OCSize.NAME,
                OCId.NAME,
                NCTrashbinFilename.NAME,
                NCTrashbinLocation.NAME,
                NCTrashbinDeletionTime.NAME
            )

        val FILE_VERSION =
            arrayOf(
                GetContentType.NAME,
                ResourceType.NAME,
                GetContentLength.NAME,
                NCGetLastModified.NAME,
                NCCreationDate.NAME,
                OCId.NAME,
                OCSize.NAME
            )

        val CHUNK =
            arrayOf(
                GetContentType.NAME,
                ResourceType.NAME,
                GetContentLength.NAME
            )
    }

    fun registerCustomFactories() {
        val list =
            listOf(
                NCCreationDate.Factory(),
                NCEncrypted.Factory(),
                GetETag.Factory(),
                NCFavorite.Factory(),
                NCGetLastModified.Factory(),
                NCHidden.Factory(),
                NCLock.Factory(),
                NCLockOwnerDisplayName.Factory(),
                NCLockOwnerEditor.Factory(),
                NCLockOwner.Factory(),
                NCLockOwnerType.Factory(),
                NCLockTime.Factory(),
                NCLockTimeout.Factory(),
                NCLockToken.Factory(),
                NCMetadataGPS.Factory(),
                NCMetadataLivePhoto.Factory(),
                NCMetadataPhotosGPS.Factory(),
                NCMetadataPhotosSize.Factory(),
                NCMetadataSize.Factory(),
                NCMountType.Factory(),
                NCNote.Factory(),
                NCPermissions.Factory(),
                NCPreview.Factory(),
                NCRichWorkspace.Factory(),
                NCSharees.Factory(),
                NCTags.Factory(),
                NCTrashbinDeletionTime.Factory(),
                NCTrashbinFilename.Factory(),
                NCTrashbinLocation.Factory(),
                NCUploadTime.Factory(),
                OCCommentsUnread.Factory(),
                OCDisplayName.Factory(),
                OCId.Factory(),
                OCLocalId.Factory(),
                OCOwnerDisplayName.Factory(),
                OCOwnerId.Factory(),
                OCSize.Factory()
            )
        PropertyRegistry.register(list)
    }

    @Suppress("LongMethod")
    fun parseResponse(
        response: Response,
        filesDavUri: Uri
    ): RemoteFile {
        val remoteFile = RemoteFile()

        val path = "/" + URLDecoder.decode(response.href.toString().substringAfter(filesDavUri.toString()), "UTF-8")

        for (property in response.properties) {
            when (property) {
                is DisplayName -> {
                    remoteFile.name = property.displayName ?: ""
                }

                is GetContentLength -> {
                    remoteFile.length = property.contentLength
                }

                is GetContentType -> {
                    remoteFile.mimeType = (property.type ?: "").toString()
                }

                is ResourceType -> {
                    if (property.types.contains(ResourceType.COLLECTION)) {
                        remoteFile.mimeType = WebdavEntry.DIR_TYPE
                    }
                }

                is NCCreationDate -> {
                    remoteFile.creationTimestamp = property.creationDate
                }

                is NCEncrypted -> {
                    remoteFile.isEncrypted = property.encrypted
                }

                is GetETag -> {
                    remoteFile.etag = property.eTag
                }

                is NCFavorite -> {
                    remoteFile.isFavorite = property.favorite
                }

                is NCGetLastModified -> {
                    remoteFile.modifiedTimestamp = property.lastModified
                }

                is NCHidden -> {
                    remoteFile.hidden = property.hidden
                }

                is NCLock -> {
                    remoteFile.isLocked = property.locked
                }

                is NCLockOwner -> {
                    remoteFile.lockOwner = property.lockOwner
                }

                is NCLockOwnerDisplayName -> {
                    remoteFile.lockOwnerDisplayName = property.lockOwnerDisplayName
                }

                is NCLockOwnerEditor -> {
                    remoteFile.lockOwnerEditor = property.lockOwnerEditor
                }

                is NCLockOwnerType -> {
                    remoteFile.lockType = property.lockOwnerType
                }

                is NCLockTime -> {
                    remoteFile.lockTimestamp = property.lockTime
                }

                is NCLockTimeout -> {
                    remoteFile.lockTimeout = property.lockTimeout
                }

                is NCLockToken -> {
                    remoteFile.lockToken = property.lockToken
                }

                is NCMetadataGPS -> {
                    remoteFile.geoLocation = property.geoLocation
                }

                is NCMetadataLivePhoto -> {
                    remoteFile.livePhoto = property.livePhoto
                }

                is NCMetadataPhotosGPS -> {
                    remoteFile.geoLocation = property.geoLocation
                }

                is NCMetadataPhotosSize -> {
                    remoteFile.imageDimension = property.imageDimension
                }

                is NCMetadataSize -> {
                    remoteFile.imageDimension = property.imageDimension
                }

                is NCMountType -> {
                    remoteFile.mountType = property.mountType
                }

                is NCNote -> {
                    remoteFile.note = property.note
                }

                is NCPermissions -> {
                    remoteFile.permissions = property.permissions
                }

                is NCPreview -> {
                    remoteFile.isHasPreview = property.preview
                }

                is NCRichWorkspace -> {
                    remoteFile.richWorkspace = property.richWorkspace
                }

                is NCSharees -> {
                    remoteFile.sharees = property.sharees
                }

                is NCTags -> {
                    remoteFile.tags = property.tags
                }

                is NCTrashbinDeletionTime -> { /* TODO */ }

                is NCTrashbinFilename -> { /* TODO */ }

                is NCTrashbinLocation -> { /* TODO */ }

                is NCUploadTime -> {
                    remoteFile.uploadTimestamp = property.uploadTime
                }

                is OCCommentsUnread -> {
                    remoteFile.unreadCommentsCount = property.commentsCount
                }

                is OCDisplayName -> {
                    remoteFile.name = property.displayName
                }

                is OCId -> {
                    remoteFile.remoteId = property.id
                }

                is OCLocalId -> {
                    remoteFile.localId = property.localId
                }

                is OCOwnerDisplayName -> {
                    remoteFile.ownerDisplayName = property.ownerDisplayName ?: ""
                }

                is OCOwnerId -> {
                    remoteFile.ownerId = property.ownerId ?: ""
                }

                is OCSize -> {
                    remoteFile.size = property.size
                }
            }
        }

        remoteFile.remotePath = path

        // displayName not set - get from path
        if (remoteFile.name?.isEmpty() == true) {
            remoteFile.name = path.substringAfterLast("/")
        }

        return remoteFile
    }
}
