/* Nextcloud Android Library is available under MIT license
*
*   @author ZetaZom
*   Copyright (C) 2024 ZetaTom
*   Copyright (C) 2024 Nextcloud GmbH
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

package com.owncloud.android.lib.common.network

import at.bitfire.dav4jvm.Property

enum class ExtendedProperties(val value: String, val namespace: String) {
    CREATION_TIME("creation_time", WebdavUtils.NAMESPACE_NC),
    FAVORITE("favorite", WebdavUtils.NAMESPACE_OC),
    HAS_PREVIEW("has-preview", WebdavUtils.NAMESPACE_NC),
    HIDDEN("hidden", WebdavUtils.NAMESPACE_NC),
    IS_ENCRYPTED("is-encrypted", WebdavUtils.NAMESPACE_NC),
    LOCK("lock", WebdavUtils.NAMESPACE_NC),
    LOCK_OWNER("lock-owner", WebdavUtils.NAMESPACE_NC),
    LOCK_OWNER_DISPLAY_NAME("lock-owner-displayname", WebdavUtils.NAMESPACE_NC),
    LOCK_OWNER_EDITOR("lock-owner-editor", WebdavUtils.NAMESPACE_NC),
    LOCK_OWNER_TYPE("lock-owner-type", WebdavUtils.NAMESPACE_NC),
    LOCK_TIME("lock-time", WebdavUtils.NAMESPACE_NC),
    LOCK_TIMEOUT("lock-timeout", WebdavUtils.NAMESPACE_NC),
    LOCK_TOKEN("lock-token", WebdavUtils.NAMESPACE_NC),
    METADATA_GPS("file-metadata-gps", WebdavUtils.NAMESPACE_NC),
    METADATA_LIVE_PHOTO("metadata-files-live-photo", WebdavUtils.NAMESPACE_NC),
    METADATA_PHOTOS_GPS("metadata-photos-gps", WebdavUtils.NAMESPACE_NC),
    METADATA_PHOTOS_SIZE("metadata-photos-size", WebdavUtils.NAMESPACE_NC),
    METADATA_SIZE("file-metadata-size", WebdavUtils.NAMESPACE_NC),
    MOUNT_TYPE("mount-type", WebdavUtils.NAMESPACE_NC),
    NAME_LOCAL_ID("fileid", WebdavUtils.NAMESPACE_OC),
    NAME_PERMISSIONS("permissions", WebdavUtils.NAMESPACE_OC),
    NAME_REMOTE_ID("id", WebdavUtils.NAMESPACE_OC),
    NAME_SIZE("size", WebdavUtils.NAMESPACE_OC),
    NOTE("note", WebdavUtils.NAMESPACE_NC),
    OWNER_DISPLAY_NAME("owner-display-name", WebdavUtils.NAMESPACE_OC),
    OWNER_ID("owner-id", WebdavUtils.NAMESPACE_OC),
    RICH_WORKSPACE("rich-workspace", WebdavUtils.NAMESPACE_NC),
    SHAREES("sharees", WebdavUtils.NAMESPACE_NC),
    SHAREES_DISPLAY_NAME("display-name", WebdavUtils.NAMESPACE_NC),
    SHAREES_ID("id", WebdavUtils.NAMESPACE_NC),
    SHAREES_SHARE_TYPE("type", WebdavUtils.NAMESPACE_NC),
    SYSTEM_TAGS("system-tags", WebdavUtils.NAMESPACE_NC),
    TRASHBIN_DELETION_TIME("trashbin-deletion-time", WebdavUtils.NAMESPACE_NC),
    TRASHBIN_FILENAME("trashbin-filename", WebdavUtils.NAMESPACE_NC),
    TRASHBIN_ORIGINAL_LOCATION("trashbin-original-location", WebdavUtils.NAMESPACE_NC),
    UNREAD_COMMENTS("comments-unread", WebdavUtils.NAMESPACE_OC),
    UPLOAD_TIME("upload_time", WebdavUtils.NAMESPACE_NC);

    fun toPropertyName(): Property.Name {
        return Property.Name(namespace, value)
    }
}