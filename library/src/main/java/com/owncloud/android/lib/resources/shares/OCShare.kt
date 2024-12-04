/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2023 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-FileCopyrightText: 2014 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-FileCopyrightText: 2014 masensio <masensio@solidgear.es>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.shares

import android.os.Parcel
import android.os.Parcelable
import com.nextcloud.android.lib.resources.files.FileDownloadLimit
import com.nextcloud.extensions.readSerializableCompat
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.files.FileUtils
import java.io.Serializable

/**
 * Contains the data of a Share from the Share API
 *
 * @author masensio
 */
@Suppress("Detekt.TooManyFunctions") // legacy code
class OCShare :
    Parcelable,
    Serializable {
    var id: Long = 0
    var fileSource: Long = 0
    var itemSource: Long = 0
    var shareType: ShareType? = null
    var shareWith: String? = null
        set(value) {
            field = value ?: ""
        }
    var path: String? = null
        set(value) {
            field = value ?: ""
        }
    var permissions = 0
    var sharedDate: Long = 0
    var expirationDate: Long = 0
    var token: String? = null
        set(value) {
            field = value ?: ""
        }
    var sharedWithDisplayName: String? = null
        set(value) {
            field = value ?: ""
        }
    var isFolder = false
    var userId: String? = null
    var remoteId: Long = 0
    var shareLink: String? = null
        set(value) {
            field = value ?: ""
        }
    var isPasswordProtected: Boolean = false
        get() {
            return if (ShareType.PUBLIC_LINK == shareType) {
                shareWith!!.isNotEmpty()
            } else {
                field
            }
        }
    var note: String? = null
    var isHideFileDownload = false
    var label: String? = null
    var isHasPreview = false
    var mimetype: String? = null
    var ownerDisplayName: String? = null
    var isFavorite = false
    var fileDownloadLimit: FileDownloadLimit? = null

    constructor() : super() {
        resetData()
    }

    constructor(path: String?) {
        resetData()
        if (path == null || path.isEmpty() || !path.startsWith(FileUtils.PATH_SEPARATOR)) {
            Log_OC.e(TAG, "Trying to create a OCShare with a non valid path")
            throw IllegalArgumentException("Trying to create a OCShare with a non valid path: $path")
        }
        this.path = path
    }

    /**
     * Used internally. Reset all file properties
     */
    private fun resetData() {
        id = -1
        fileSource = 0
        itemSource = 0
        shareType = ShareType.NO_SHARED
        shareWith = ""
        path = ""
        permissions = -1
        sharedDate = 0
        expirationDate = 0
        token = ""
        sharedWithDisplayName = ""
        isFolder = false
        userId = ""
        remoteId = -1
        shareLink = ""
        isPasswordProtected = false
        note = ""
        isHideFileDownload = false
        label = ""
        isHasPreview = false
        mimetype = ""
        ownerDisplayName = ""
        fileDownloadLimit = null
    }

    /**
     * Reconstruct from parcel
     *
     * @param source The source parcel
     */
    private constructor(source: Parcel) {
        readFromParcel(source)
    }

    @Suppress("Detekt.SwallowedException") // legacy code
    fun readFromParcel(source: Parcel) {
        id = source.readLong()
        fileSource = source.readLong()
        itemSource = source.readLong()
        shareType =
            try {
                ShareType.valueOf(source.readString()!!)
            } catch (x: IllegalArgumentException) {
                ShareType.NO_SHARED
            }
        shareWith = source.readString()
        path = source.readString()
        permissions = source.readInt()
        sharedDate = source.readLong()
        expirationDate = source.readLong()
        token = source.readString()
        sharedWithDisplayName = source.readString()
        isFolder = source.readInt() == 0
        userId = source.readString()
        remoteId = source.readLong()
        shareLink = source.readString()
        isPasswordProtected = source.readInt() == 1
        isHideFileDownload = source.readInt() == 1
        label = source.readString()
        isHasPreview = source.readInt() == 1
        mimetype = source.readString()
        ownerDisplayName = source.readString()
        fileDownloadLimit = source.readSerializableCompat()
    }

    override fun describeContents(): Int = this.hashCode()

    override fun writeToParcel(
        dest: Parcel,
        flags: Int
    ) {
        dest.writeLong(id)
        dest.writeLong(fileSource)
        dest.writeLong(itemSource)
        dest.writeString(if (shareType == null) "" else shareType!!.name)
        dest.writeString(shareWith)
        dest.writeString(path)
        dest.writeInt(permissions)
        dest.writeLong(sharedDate)
        dest.writeLong(expirationDate)
        dest.writeString(token)
        dest.writeString(sharedWithDisplayName)
        dest.writeInt(if (isFolder) 1 else 0)
        dest.writeString(userId)
        dest.writeLong(remoteId)
        dest.writeString(shareLink)
        dest.writeInt(if (isPasswordProtected) 1 else 0)
        dest.writeInt(if (isHideFileDownload) 1 else 0)
        dest.writeString(label)
        dest.writeInt(if (isHasPreview) 1 else 0)
        dest.writeString(mimetype)
        dest.writeString(ownerDisplayName)
        dest.writeSerializable(fileDownloadLimit)
    }

    companion object {
        /**
         * Generated - should be refreshed every time the class changes!!
         */
        private const val serialVersionUID = 6725469882304546557L
        private val TAG = OCShare::class.java.simpleName
        const val NO_PERMISSION = -1
        const val READ_PERMISSION_FLAG = 1
        const val UPDATE_PERMISSION_FLAG = 2
        const val CREATE_PERMISSION_FLAG = 4
        const val DELETE_PERMISSION_FLAG = 8
        const val SHARE_PERMISSION_FLAG = 16

        const val MAXIMUM_PERMISSIONS_FOR_FILE =
            READ_PERMISSION_FLAG +
                UPDATE_PERMISSION_FLAG +
                SHARE_PERMISSION_FLAG
        const val MAXIMUM_PERMISSIONS_FOR_FOLDER =
            MAXIMUM_PERMISSIONS_FOR_FILE +
                CREATE_PERMISSION_FLAG +
                DELETE_PERMISSION_FLAG
        const val FEDERATED_PERMISSIONS_FOR_FILE =
            READ_PERMISSION_FLAG +
                UPDATE_PERMISSION_FLAG +
                CREATE_PERMISSION_FLAG +
                DELETE_PERMISSION_FLAG
        const val FEDERATED_PERMISSIONS_FOR_FOLDER =
            READ_PERMISSION_FLAG +
                UPDATE_PERMISSION_FLAG +
                CREATE_PERMISSION_FLAG +
                DELETE_PERMISSION_FLAG +
                SHARE_PERMISSION_FLAG

        /**
         * Parcelable Methods
         */
        @JvmField
        val CREATOR: Parcelable.Creator<OCShare> =
            object : Parcelable.Creator<OCShare> {
                override fun createFromParcel(source: Parcel): OCShare = OCShare(source)

                override fun newArray(size: Int): Array<OCShare?> = arrayOfNulls(size)
            }
    }
}
