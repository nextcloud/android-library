/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2016-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-FileCopyrightText: 2023 Álvaro Brey <alvaro.brey@nextcloud.com>
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.network

import android.net.Uri
import com.google.gson.Gson
import com.nextcloud.extensions.fromDavProperty
import com.nextcloud.extensions.processXmlData
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.files.model.FileLockType
import com.owncloud.android.lib.resources.files.model.FileLockType.Companion.fromValue
import com.owncloud.android.lib.resources.files.model.GeoLocation
import com.owncloud.android.lib.resources.files.model.ImageDimension
import com.owncloud.android.lib.resources.shares.ShareType
import com.owncloud.android.lib.resources.shares.ShareeUser
import org.apache.jackrabbit.webdav.MultiStatusResponse
import org.apache.jackrabbit.webdav.property.DavProperty
import org.apache.jackrabbit.webdav.property.DavPropertyName
import org.apache.jackrabbit.webdav.property.DavPropertySet
import org.apache.jackrabbit.webdav.xml.Namespace
import org.w3c.dom.Element
import java.math.BigDecimal

@Suppress("Detekt.TooGenericExceptionCaught") // legacy code
class WebdavEntry(ms: MultiStatusResponse, splitElement: String) {
    var name: String? = null
        private set
    var path: String? = null
    var uri: String? = null
        private set
    var contentType: String? = null
        private set
    var eTag: String? = null
    var permissions: String? = null
        private set
    var remoteId: String? = null
        private set
    var localId: Long = 0
        private set
    var trashbinOriginalLocation: String? = null
    var trashbinFilename: String? = null
    var trashbinDeletionTimestamp: Long = 0
    var isFavorite = false
    var isEncrypted = false
    var mountType: MountType? = null
    var contentLength: Long = 0
        private set
    var createTimestamp: Long = 0
        private set
    var modifiedTimestamp: Long = 0
        private set
    var uploadTimestamp: Long = 0
    var size: Long = 0
        private set
    var quotaUsedBytes: BigDecimal? = null
        private set
    var quotaAvailableBytes: BigDecimal? = null
        private set
    var ownerId: String? = null
    var ownerDisplayName: String? = null
    var unreadCommentsCount = 0
    var isHasPreview = false
    var note = ""
    var sharees = arrayOf<ShareeUser>()
    var richWorkspace: String? = null
    var isLocked = false
        private set
    var lockOwnerType: FileLockType? = null
        private set
    var lockOwnerId: String? = null
        private set
    var lockOwnerDisplayName: String? = null
        private set
    var lockTimestamp: Long = 0
        private set
    var lockOwnerEditor: String? = null
        private set
    var lockTimeout: Long = 0
        private set
    var lockToken: String? = null
        private set
    var tags = arrayOf<String>()
    var imageDimension: ImageDimension? = null
    var geoLocation: GeoLocation? = null
    var hidden = false
        private set
    var livePhoto: String? = null
        private set

    private val gson = Gson()

    enum class MountType {
        INTERNAL,
        EXTERNAL,
        GROUP
    }

    init {
        resetData()
        if (ms.status.isNotEmpty()) {
            uri = ms.href
            path =
                uri!!.split(splitElement.toRegex(), limit = 2).toTypedArray()[1].replace("//", "/")
            var status = ms.status[0].statusCode
            if (status == CODE_PROP_NOT_FOUND) {
                status = ms.status[1].statusCode
            }
            val propSet = ms.getProperties(status)
            var prop = propSet[DavPropertyName.DISPLAYNAME]
            if (prop != null) {
                name = prop.name.toString()
                name = name!!.substring(1, name!!.length - 1)
            } else {
                val tmp = path!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (tmp.isNotEmpty()) name = tmp[tmp.size - 1]
            }

            // use unknown mimetype as default behavior
            // {DAV:}getcontenttype
            contentType = "application/octet-stream"
            prop = propSet[DavPropertyName.GETCONTENTTYPE]
            if (prop != null) {
                val contentType = prop.value as String?
                // dvelasco: some builds of ownCloud server 4.0.x added a trailing ';'
                // to the MIME type ; if looks fixed, but let's be cautious
                contentType?.let {
                    this.contentType = it.substringBefore(";")
                }
            }

            // check if it's a folder in the standard way: see RFC2518 12.2 . RFC4918 14.3
            // {DAV:}resourcetype
            prop = propSet[DavPropertyName.RESOURCETYPE]
            if (prop != null) {
                val value = prop.value
                if (value != null) {
                    contentType = DIR_TYPE // a specific attribute would be better,
                    // but this is enough;
                    // unless while we have no reason to distinguish
                    // MIME types for folders
                }
            }

            // {DAV:}getcontentlength
            prop = propSet[DavPropertyName.GETCONTENTLENGTH]
            if (prop != null) {
                contentLength = (prop.value as String).toLong()
            }

            // {DAV:}getlastmodified
            prop = propSet[DavPropertyName.GETLASTMODIFIED]
            if (prop != null) {
                val d = WebdavUtils.parseResponseDate(prop.value as String)
                modifiedTimestamp = d?.time ?: 0
            }

            // {NS:} creation_time
            prop = getDavProp(propSet, ExtendedProperties.CREATION_TIME)
            if (prop != null) {
                createTimestamp =
                    try {
                        (prop.value as String).toLong()
                    } catch (e: NumberFormatException) {
                        0
                    }
            }

            // {NS:} upload_time
            prop = getDavProp(propSet, ExtendedProperties.UPLOAD_TIME)
            if (prop != null) {
                uploadTimestamp =
                    try {
                        (prop.value as String).toLong()
                    } catch (e: NumberFormatException) {
                        0
                    }
            }

            // {DAV:}getetag
            prop = propSet[DavPropertyName.GETETAG]
            if (prop != null) {
                eTag = prop.value as String
                eTag = WebdavUtils.parseEtag(eTag)
            }

            // {DAV:}quota-used-bytes
            prop = propSet[DavPropertyName.create(PROPERTY_QUOTA_USED_BYTES)]
            if (prop != null) {
                val quotaUsedBytesSt = prop.value as String
                try {
                    quotaUsedBytes = BigDecimal(quotaUsedBytesSt)
                } catch (e: NumberFormatException) {
                    Log_OC.w(TAG, "No value for QuotaUsedBytes - NumberFormatException")
                } catch (e: NullPointerException) {
                    Log_OC.w(TAG, "No value for QuotaUsedBytes - NullPointerException")
                }
                Log_OC.d(TAG, "QUOTA_USED_BYTES $quotaUsedBytesSt")
            }

            // {DAV:}quota-available-bytes
            prop = propSet[DavPropertyName.create(PROPERTY_QUOTA_AVAILABLE_BYTES)]
            if (prop != null) {
                val quotaAvailableBytesSt = prop.value as String
                try {
                    quotaAvailableBytes = BigDecimal(quotaAvailableBytesSt)
                } catch (e: NumberFormatException) {
                    Log_OC.w(TAG, "No value for QuotaAvailableBytes - NumberFormatException")
                } catch (e: NullPointerException) {
                    Log_OC.w(TAG, "No value for QuotaAvailableBytes")
                }
                Log_OC.d(TAG, "QUOTA_AVAILABLE_BYTES $quotaAvailableBytesSt")
            }

            // OC permissions property <oc:permissions>
            prop = getDavProp(propSet, ExtendedProperties.NAME_PERMISSIONS)
            if (prop != null && prop.value != null) {
                permissions = prop.value.toString()
            }

            // OC remote id property <oc:id>
            prop = getDavProp(propSet, ExtendedProperties.NAME_REMOTE_ID)
            if (prop != null) {
                remoteId = prop.value.toString()
            }

            // OC remote id property <oc:fileid>
            prop = getDavProp(propSet, ExtendedProperties.NAME_LOCAL_ID)
            if (prop != null) {
                localId = (prop.value as String).toLong()
            }

            // OC size property <oc:size>
            prop = getDavProp(propSet, ExtendedProperties.NAME_SIZE)
            if (prop != null) {
                size = (prop.value as String).toLong()
            }

            // OC favorite property <oc:favorite>
            prop = getDavProp(propSet, ExtendedProperties.FAVORITE)
            isFavorite =
                if (prop != null) {
                    val favoriteValue = prop.value as String
                    IS_ENCRYPTED == favoriteValue
                } else {
                    false
                }

            // NC encrypted property <nc:is-encrypted>
            prop = getDavProp(propSet, ExtendedProperties.IS_ENCRYPTED)
            isEncrypted =
                if (prop != null) {
                    val encryptedValue = prop.value as String
                    IS_ENCRYPTED == encryptedValue
                } else {
                    false
                }

            // NC mount-type property <nc:mount-type>
            prop = getDavProp(propSet, ExtendedProperties.MOUNT_TYPE)
            mountType =
                if (prop != null) {
                    when (prop.value) {
                        "external" -> {
                            MountType.EXTERNAL
                        }

                        "group" -> {
                            MountType.GROUP
                        }

                        else -> {
                            MountType.INTERNAL
                        }
                    }
                } else {
                    MountType.INTERNAL
                }

            // OC owner-id property <oc:owner-id>
            prop = getDavProp(propSet, ExtendedProperties.OWNER_ID)
            ownerId =
                if (prop != null) {
                    prop.value as String
                } else {
                    ""
                }

            // OC owner-display-name property <oc:owner-display-name>
            prop = getDavProp(propSet, ExtendedProperties.OWNER_DISPLAY_NAME)
            ownerDisplayName =
                if (prop != null) {
                    prop.value as String
                } else {
                    ""
                }

            // OC unread comments property <oc-comments-unread>
            prop = getDavProp(propSet, ExtendedProperties.UNREAD_COMMENTS)
            unreadCommentsCount =
                if (prop != null) {
                    Integer.valueOf(prop.value.toString())
                } else {
                    0
                }

            // NC has preview property <nc-has-preview>
            prop = getDavProp(propSet, ExtendedProperties.HAS_PREVIEW)
            isHasPreview =
                if (prop != null) {
                    java.lang.Boolean.valueOf(prop.value.toString())
                } else {
                    true
                }

            // NC trashbin-original-location <nc:trashbin-original-location>
            prop = getDavProp(propSet, ExtendedProperties.TRASHBIN_ORIGINAL_LOCATION)
            if (prop != null) {
                trashbinOriginalLocation = prop.value.toString()
            }

            // NC trashbin-filename <nc:trashbin-filename>
            prop = getDavProp(propSet, ExtendedProperties.TRASHBIN_FILENAME)
            if (prop != null) {
                trashbinFilename = prop.value.toString()
            }

            // NC trashbin-deletion-time <nc:trashbin-deletion-time>
            prop = getDavProp(propSet, ExtendedProperties.TRASHBIN_DELETION_TIME)
            if (prop != null) {
                trashbinDeletionTimestamp = (prop.value as String).toLong()
            }

            // NC note property <nc:note>
            prop = getDavProp(propSet, ExtendedProperties.NOTE)
            if (prop != null && prop.value != null) {
                note = prop.value.toString()
            }

            // NC rich-workspace property <nc:rich-workspace>
            // can be null if rich-workspace is disabled for this user
            prop = getDavProp(propSet, ExtendedProperties.RICH_WORKSPACE)
            richWorkspace =
                if (prop != null) {
                    if (prop.value != null) {
                        prop.value.toString()
                    } else {
                        ""
                    }
                } else {
                    null
                }

            // NC sharees property <nc-sharees>
            prop = getDavProp(propSet, ExtendedProperties.SHAREES)
            if (prop != null && prop.value != null) {
                if (prop.value is ArrayList<*>) {
                    val list = prop.value as ArrayList<*>
                    val tempList: MutableList<ShareeUser> = ArrayList()
                    for (element in list) {
                        val user = createShareeUser(element as Element)
                        if (user != null) {
                            tempList.add(user)
                        }
                    }
                    sharees = tempList.toTypedArray()
                } else {
                    // single item or empty
                    val element = prop.value as Element
                    val user = createShareeUser(element)
                    if (user != null) {
                        sharees = arrayOf(user)
                    }
                }
            }

            prop = getDavProp(propSet, ExtendedProperties.SYSTEM_TAGS)
            if (prop != null && prop.value != null) {
                if (prop.value is ArrayList<*>) {
                    val list = prop.value as ArrayList<*>
                    val tempList: MutableList<String> = ArrayList(list.size)
                    for (i in list.indices) {
                        val element = list[i] as Element
                        tempList.add(element.firstChild.textContent)
                    }
                    tags = tempList.toTypedArray()
                } else {
                    // single item or empty
                    val element = prop.value as Element
                    val value = element.firstChild.textContent

                    if (value != null) {
                        tags = arrayOf(value)
                    }
                }
            }

            // NC metadata size property <nc:file-metadata-size>
            prop = getDavProp(propSet, ExtendedProperties.METADATA_SIZE)
            imageDimension =
                if (prop == null) {
                    prop = getDavProp(propSet, ExtendedProperties.METADATA_SIZE)
                    gson.fromDavProperty<ImageDimension>(prop)
                } else {
                    val xmlData = prop.value as? ArrayList<*>
                    val width = xmlData?.processXmlData<Float>("width")
                    val height = xmlData?.processXmlData<Float>("height")

                    if (width != null && height != null) {
                        ImageDimension(width, height)
                    } else {
                        prop = getDavProp(propSet, ExtendedProperties.METADATA_SIZE)
                        gson.fromDavProperty<ImageDimension>(prop)
                    }
                }

            // NC metadata gps property <nc:file-metadata-gps>
            prop = getDavProp(propSet, ExtendedProperties.METADATA_PHOTOS_GPS)
            geoLocation =
                if (prop == null) {
                    prop = getDavProp(propSet, ExtendedProperties.METADATA_GPS)
                    gson.fromDavProperty<GeoLocation>(prop)
                } else {
                    val xmlData = prop.value as? ArrayList<*>
                    val latitude = xmlData?.processXmlData<Double>("latitude")
                    val longitude = xmlData?.processXmlData<Double>("longitude")

                    if (latitude != null && longitude != null) {
                        GeoLocation(latitude, longitude)
                    } else {
                        prop = getDavProp(propSet, ExtendedProperties.METADATA_GPS)
                        gson.fromDavProperty<GeoLocation>(prop)
                    }
                }

            // NC metadata live photo property: <nc:metadata-files-live-photo/>
            prop = getDavProp(propSet, ExtendedProperties.METADATA_LIVE_PHOTO)
            if (prop != null && prop.value != null) {
                livePhoto = prop.value.toString()
            }

            // NC has hidden property <nc:hidden>
            prop = getDavProp(propSet, ExtendedProperties.HIDDEN)
            hidden =
                if (prop != null) {
                    java.lang.Boolean.valueOf(prop.value.toString())
                } else {
                    false
                }

            parseLockProperties(propSet)
        } else {
            Log_OC.e("WebdavEntry", "General error, no status for webdav response")
        }
    }

    private fun parseLockProperties(propSet: DavPropertySet) {
        // file locking
        var prop = getDavProp(propSet, ExtendedProperties.LOCK)
        isLocked =
            if (prop != null && prop.value != null) {
                "1" == prop.value as String
            } else {
                false
            }
        prop = getDavProp(propSet, ExtendedProperties.LOCK_OWNER_TYPE)
        lockOwnerType =
            if (prop != null && prop.value != null) {
                val value: Int = (prop.value as String).toInt()
                fromValue(value)
            } else {
                null
            }
        lockOwnerId = parseStringProp(getDavProp(propSet, ExtendedProperties.LOCK_OWNER))
        lockOwnerDisplayName = parseStringProp(getDavProp(propSet, ExtendedProperties.LOCK_OWNER_DISPLAY_NAME))
        lockOwnerEditor = parseStringProp(getDavProp(propSet, ExtendedProperties.LOCK_OWNER_EDITOR))
        lockTimestamp = parseStringProp(getDavProp(propSet, ExtendedProperties.LOCK_TIME))?.toLong() ?: 0L
        lockTimeout = parseStringProp(getDavProp(propSet, ExtendedProperties.LOCK_TIMEOUT))?.toLong() ?: 0L
        lockToken = parseStringProp(getDavProp(propSet, ExtendedProperties.LOCK_TOKEN))
    }

    private fun parseStringProp(prop: DavProperty<*>?): String? {
        return prop?.value as String?
    }

    private fun createShareeUser(element: Element): ShareeUser? {
        val displayName = extractDisplayName(element)
        val userId = extractUserId(element)
        val shareType = extractShareType(element)
        val isSupportedShareType =
            ShareType.EMAIL == shareType ||
                ShareType.FEDERATED == shareType ||
                ShareType.GROUP == shareType ||
                ShareType.ROOM == shareType
        return if ((isSupportedShareType || displayName.isNotEmpty()) && userId.isNotEmpty()) {
            ShareeUser(userId, displayName, shareType)
        } else {
            null
        }
    }

    private fun extractDisplayName(element: Element): String {
        val displayName =
            element.getElementsByTagNameNS(
                ExtendedProperties.SHAREES_DISPLAY_NAME.namespace,
                ExtendedProperties.SHAREES_DISPLAY_NAME.value
            ).item(0)
        return if (displayName != null && displayName.firstChild != null) {
            displayName.firstChild.nodeValue
        } else {
            ""
        }
    }

    private fun extractUserId(element: Element): String {
        val userId =
            element.getElementsByTagNameNS(
                ExtendedProperties.SHAREES_ID.namespace,
                ExtendedProperties.SHAREES_ID.value
            ).item(0)
        return if (userId != null && userId.firstChild != null) {
            userId.firstChild.nodeValue
        } else {
            ""
        }
    }

    private fun extractShareType(element: Element): ShareType {
        val shareType =
            element.getElementsByTagNameNS(
                ExtendedProperties.SHAREES_SHARE_TYPE.namespace,
                ExtendedProperties.SHAREES_SHARE_TYPE.value
            ).item(0)
        if (shareType != null && shareType.firstChild != null) {
            val value = shareType.firstChild.nodeValue.toInt()
            return ShareType.fromValue(value)
        }
        return ShareType.NO_SHARED
    }

    fun decodedPath(): String {
        return Uri.decode(path)
    }

    val isDirectory: Boolean
        get() = DIR_TYPE == contentType

    private fun resetData() {
        permissions = null
        contentType = permissions
        uri = contentType
        name = uri
        remoteId = null
        localId = -1
        modifiedTimestamp = 0
        createTimestamp = modifiedTimestamp
        contentLength = createTimestamp
        size = 0
        quotaUsedBytes = null
        quotaAvailableBytes = null
        isFavorite = false
        isHasPreview = false
    }

    /**
     * Return dav property for given extended property via propSet.
     *
     * remove - only intended as a transitional aid
     */
    private fun getDavProp(
        propSet: DavPropertySet,
        extendedProperty: ExtendedProperties
    ): DavProperty<*>? {
        return propSet[extendedProperty.value, Namespace.getNamespace(extendedProperty.namespace)]
    }

    companion object {
        private val TAG = WebdavEntry::class.java.simpleName
        private const val IS_ENCRYPTED = "1"
        private const val CODE_PROP_NOT_FOUND = 404
        const val PROPERTY_QUOTA_USED_BYTES = "quota-used-bytes"
        const val PROPERTY_QUOTA_AVAILABLE_BYTES = "quota-available-bytes"
        const val DIR_TYPE = "DIR"
    }
}
