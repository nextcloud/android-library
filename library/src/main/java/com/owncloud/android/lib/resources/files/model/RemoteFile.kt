/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files.model

import android.os.Parcel
import android.os.Parcelable
import com.nextcloud.extensions.readParcelableArrayCompat
import com.nextcloud.extensions.readSerializableCompat
import com.owncloud.android.lib.common.network.WebdavEntry
import com.owncloud.android.lib.common.network.WebdavEntry.MountType
import com.owncloud.android.lib.resources.files.FileUtils
import com.owncloud.android.lib.resources.files.model.FileLockType.Companion.fromValue
import com.owncloud.android.lib.resources.shares.ShareeUser
import java.io.Serializable

/**
 * Contains the data of a Remote File from a WebDavEntry.
 *
 * @author masensio
 */
class RemoteFile() : Parcelable, Serializable {
    var remotePath: String? = null
    var mimeType: String? = null
    var length: Long = 0
    var creationTimestamp: Long = 0
    var modifiedTimestamp: Long = 0
    var uploadTimestamp: Long = 0
    var etag: String? = null
    var permissions: String? = null
    var localId: Long = -1
    var remoteId: String? = null
    var size: Long = 0
    var isFavorite = false
    var isEncrypted = false
    var mountType: MountType? = null
    var ownerId: String = ""
    var ownerDisplayName: String = ""
    var unreadCommentsCount = 0
    var isHasPreview = false
    var name: String = ""
    var note: String = ""
    var sharees: Array<ShareeUser>? = null
    var richWorkspace: String? = null
    var isLocked = false
    var lockType: FileLockType? = null
    var lockOwner: String? = null
    var lockOwnerDisplayName: String? = null
    var lockTimestamp: Long = 0
    var lockOwnerEditor: String? = null
    var lockTimeout: Long = 0
    var lockToken: String? = null
    var tags: Array<String>? = null
    var imageDimension: ImageDimension? = null
    var geoLocation: GeoLocation? = null
    var hidden = false
    var livePhoto: String? = null

    /**
     * Create new [RemoteFile] with given path.
     *
     * The path received must be URL-decoded. Path separator must be OCFile.PATH_SEPARATOR, and it
     * must be the first character in 'path'.
     *
     * @param path The remote path of the file.
     */
    constructor(path: String?) : this() {
        require(!(path.isNullOrEmpty() || !path.startsWith(FileUtils.PATH_SEPARATOR))) {
            "Trying to create a OCFile with a non valid remote path: $path"
        }
        remotePath = path
    }

    constructor(we: WebdavEntry) : this(we.decodedPath()) {
        creationTimestamp = we.createTimestamp
        length = we.contentLength
        mimeType = we.contentType
        modifiedTimestamp = we.modifiedTimestamp
        uploadTimestamp = we.uploadTimestamp
        etag = we.eTag
        permissions = we.permissions
        localId = we.localId
        remoteId = we.remoteId
        size = we.size
        isFavorite = we.isFavorite
        isEncrypted = we.isEncrypted
        mountType = we.mountType
        ownerId = we.ownerId ?: ""
        ownerDisplayName = we.ownerDisplayName ?: ""
        name = we.name ?: ""
        note = we.note
        unreadCommentsCount = we.unreadCommentsCount
        isHasPreview = we.isHasPreview
        sharees = we.sharees
        richWorkspace = we.richWorkspace
        isLocked = we.isLocked
        lockType = we.lockOwnerType
        lockOwner = we.lockOwnerId
        lockOwnerDisplayName = we.lockOwnerDisplayName
        lockOwnerEditor = we.lockOwnerEditor
        lockTimestamp = we.lockTimestamp
        lockTimeout = we.lockTimeout
        lockToken = we.lockToken
        tags = we.tags
        imageDimension = we.imageDimension
        geoLocation = we.geoLocation
        livePhoto = we.livePhoto
        hidden = we.hidden
    }

    /**
     * Reconstruct from parcel
     *
     * @param source The source parcel
     */
    private constructor(source: Parcel) : this() {
        readFromParcel(source)
    }

    private fun readFromParcel(source: Parcel) {
        creationTimestamp = source.readLong()
        etag = source.readString()
        hidden = source.readInt() == 1
        isEncrypted = source.readString().toBoolean()
        isFavorite = source.readString().toBoolean()
        isHasPreview = source.readString().toBoolean()
        isLocked = source.readInt() == 1
        length = source.readLong()
        livePhoto = source.readString()
        localId = source.readLong()
        lockOwnerDisplayName = source.readString()
        lockOwnerEditor = source.readString()
        lockOwner = source.readString()
        lockTimeout = source.readLong()
        lockTimestamp = source.readLong()
        lockToken = source.readString()
        lockType = fromValue(source.readInt())
        mimeType = source.readString()
        modifiedTimestamp = source.readLong()
        mountType = source.readSerializableCompat(MountType::class.java)
        name = source.readString() ?: ""
        note = source.readString() ?: ""
        ownerDisplayName = source.readString() ?: ""
        ownerId = source.readString() ?: ""
        permissions = source.readString()
        remoteId = source.readString()
        remotePath = source.readString()
        sharees = source.readParcelableArrayCompat(ShareeUser::class.java)
        size = source.readLong()
    }

    override fun describeContents(): Int {
        return this.hashCode()
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int
    ) {
        dest.writeString(remotePath)
        dest.writeString(mimeType)
        dest.writeLong(length)
        dest.writeLong(creationTimestamp)
        dest.writeLong(modifiedTimestamp)
        dest.writeString(etag)
        dest.writeString(permissions)
        dest.writeLong(localId)
        dest.writeString(remoteId)
        dest.writeLong(size)
        dest.writeString(isFavorite.toString())
        dest.writeString(isEncrypted.toString())
        dest.writeSerializable(mountType)
        dest.writeString(ownerId)
        dest.writeString(ownerDisplayName)
        dest.writeString(isHasPreview.toString())
        dest.writeString(name)
        dest.writeString(note)
        dest.writeParcelableArray(sharees, 0)
        dest.writeInt(if (isLocked) 1 else 0)
        dest.writeInt(if (lockType != null) lockType!!.value else -1)
        dest.writeString(lockOwner)
        dest.writeString(lockOwnerDisplayName)
        dest.writeString(lockOwnerEditor)
        dest.writeLong(lockTimestamp)
        dest.writeLong(lockTimeout)
        dest.writeString(lockToken)
        dest.writeString(livePhoto)
        dest.writeInt(if (hidden) 1 else 0)
    }

    /**
     * Check whether RemoteFile is a directory.
     *
     * @return `true`, iff RemoteFile is directory.
     */
    fun isDirectory() = mimeType == WebdavEntry.DIR_TYPE

    companion object {
        /**
         * Generated - should be refreshed every time the class changes!!
         */
        private const val serialVersionUID = -1754995094462979800L

        /**
         * Parcelable Methods
         */
        @JvmField
        val CREATOR: Parcelable.Creator<RemoteFile> =
            object : Parcelable.Creator<RemoteFile> {
                override fun createFromParcel(source: Parcel): RemoteFile {
                    return RemoteFile(source)
                }

                override fun newArray(size: Int): Array<RemoteFile?> {
                    return arrayOfNulls(size)
                }
            }
    }
}
