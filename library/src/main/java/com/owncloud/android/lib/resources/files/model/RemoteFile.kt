/* ownCloud Android Library is available under MIT license
 *   Copyright (C) 2015 ownCloud Inc.
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
package com.owncloud.android.lib.resources.files.model

import android.os.Parcel
import android.os.Parcelable
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
class RemoteFile : Parcelable, Serializable {
    var remotePath: String? = null
    var mimeType: String? = null
    var length: Long = 0
    var creationTimestamp: Long = 0
    var modifiedTimestamp: Long = 0
    var uploadTimestamp: Long = 0
    var etag: String? = null
    var permissions: String? = null
    var localId: Long = 0
    var remoteId: String? = null
    var size: Long = 0
    var isFavorite = false
    var hidden = false
    var isEncrypted = false
    var mountType: MountType? = null
    var ownerId: String? = null
    var ownerDisplayName: String? = null
    var unreadCommentsCount = 0
    var isHasPreview = false
    var note: String? = null
    var sharees: Array<ShareeUser?>? = null
    var richWorkspace: String? = null
    var isLocked = false
    var lockType: FileLockType? = null
    var lockOwner: String? = null
    var lockOwnerDisplayName: String? = null
    var lockTimestamp: Long = 0
    var lockOwnerEditor: String? = null
    var lockTimeout: Long = 0
    var lockToken: String? = null
    var tags: Array<String?>? = null
    var imageDimension: ImageDimension? = null
    var geoLocation: GeoLocation? = null
    var livePhoto: String? = null

    constructor() {
        resetData()
    }

    /**
     * Create new [RemoteFile] with given path.
     *
     * The path received must be URL-decoded. Path separator must be OCFile.PATH_SEPARATOR, and it
     * must be the first character in 'path'.
     *
     * @param path The remote path of the file.
     */
    constructor(path: String?) {
        resetData()
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
        ownerId = we.ownerId
        ownerDisplayName = we.ownerDisplayName
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
     * Used internally. Reset all file properties
     */
    private fun resetData() {
        remotePath = null
        mimeType = null
        length = 0
        creationTimestamp = 0
        modifiedTimestamp = 0
        etag = null
        permissions = null
        localId = -1
        remoteId = null
        size = 0
        isFavorite = false
        hidden = false
        isEncrypted = false
        ownerId = ""
        ownerDisplayName = ""
        note = ""
        isLocked = false
        lockOwner = null
        lockType = null
        lockOwnerDisplayName = null
        lockOwnerEditor = null
        lockTimestamp = 0
        lockTimeout = 0
        lockToken = null
        livePhoto = null
        tags = null
    }

    /**
     * Reconstruct from parcel
     *
     * @param source The source parcel
     */
    private constructor(source: Parcel) {
        readFromParcel(source)
    }

    private fun readFromParcel(source: Parcel) {
        remotePath = source.readString()
        mimeType = source.readString()
        length = source.readLong()
        creationTimestamp = source.readLong()
        modifiedTimestamp = source.readLong()
        etag = source.readString()
        permissions = source.readString()
        localId = source.readLong()
        remoteId = source.readString()
        size = source.readLong()
        isFavorite = source.readString().toBoolean()
        isEncrypted = source.readString().toBoolean()
        mountType = source.readSerializable() as MountType?
        ownerId = source.readString()
        ownerDisplayName = source.readString()
        isHasPreview = source.readString().toBoolean()
        note = source.readString()
        source.readParcelableArray(ShareeUser::class.java.classLoader)
        isLocked = source.readInt() == 1
        lockType = fromValue(source.readInt())
        lockOwner = source.readString()
        lockOwnerDisplayName = source.readString()
        lockOwnerEditor = source.readString()
        lockTimestamp = source.readLong()
        lockTimeout = source.readLong()
        lockToken = source.readString()
        livePhoto = source.readString()
        hidden = source.readInt() == 1
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
