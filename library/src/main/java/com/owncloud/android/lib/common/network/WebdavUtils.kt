/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 ZetaTom <70907959+ZetaTom@users.noreply.github.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.common.network

import android.net.Uri
import at.bitfire.dav4jvm.PropertyRegistry.register
import at.bitfire.dav4jvm.property.webdav.CreationDate
import at.bitfire.dav4jvm.property.webdav.DisplayName
import at.bitfire.dav4jvm.property.webdav.GetContentLength
import at.bitfire.dav4jvm.property.webdav.GetContentType
import at.bitfire.dav4jvm.property.webdav.ResourceType
import com.google.gson.Gson
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
import org.apache.commons.httpclient.HttpMethod
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WebdavUtils {
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
                CreationDate.NAME,
                NCEtag.NAME,
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
                NCCreationTime.NAME,
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
                CreationDate.NAME,
                NCEtag.NAME,
                NCPermissions.NAME,
                OCLocalId.NAME,
                OCId.NAME,
                OCSize.NAME,
                NCFavorite.NAME,
                NCPreview.NAME,
                NCSharees.NAME,
                NCCreationTime.NAME,
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
                CreationDate.NAME,
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

    fun parseResponseDate(date: String?): Date? {
        for (format in DATETIME_FORMATS) {
            try {
                date?.let { return format.parse(it) }
            } catch (e: ParseException) {
                // wrong format
            }
        }
        return null
    }

    /**
     * Encodes a path according to URI RFC 2396.
     *
     *
     * If the received path doesn't start with "/", the method adds it.
     *
     * @param remoteFilePath Path
     * @return Encoded path according to RFC 2396, always starting with "/"
     */
    fun encodePath(remoteFilePath: String?): String {
        val encodedPath = Uri.encode(remoteFilePath, "/")
        if (!encodedPath.startsWith("/")) {
            return "/$encodedPath"
        }
        return encodedPath
    }

    fun parseEtag(etag: String?): String {
        if (etag.isNullOrEmpty()) {
            return ""
        }
        return etag.removeSuffix("-gzip").removeSurrounding("\"")
    }

    fun getEtagFromResponse(method: HttpMethod): String {
        var eTag = method.getResponseHeader("OC-ETag")
        if (eTag == null) {
            eTag = method.getResponseHeader("oc-etag")
        }
        if (eTag == null) {
            eTag = method.getResponseHeader("ETag")
        }
        if (eTag == null) {
            eTag = method.getResponseHeader("etag")
        }
        if (eTag != null) {
            return parseEtag(eTag.value)
        }
        return ""
    }

    fun registerCustomFactories() {
        val list =
            listOf(
                NCCreationTime.Factory(),
                NCEncrypted.Factory(),
                NCEtag.Factory(),
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
        register(list)
    }
}
