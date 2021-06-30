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

package com.owncloud.android.lib.resources.files.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.owncloud.android.lib.common.network.WebdavEntry;
import com.owncloud.android.lib.resources.files.FileUtils;
import com.owncloud.android.lib.resources.shares.ShareeUser;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * Contains the data of a Remote File from a WebDavEntry.
 *
 * @author masensio
 */
@Getter
@Setter
public class RemoteFile implements Parcelable, Serializable {
    /**
     * Generated - should be refreshed every time the class changes!!
     */
    private static final long serialVersionUID = 3130865437811248451L;

    private String remotePath;
    private String mimeType;
    private long length;
    private long creationTimestamp;
    private long modifiedTimestamp;
    private long uploadTimestamp;
    private String etag;
    private String permissions;
    private long localId;
    private String remoteId;
    private long size;
    private boolean favorite;
    private boolean encrypted;
    private WebdavEntry.MountType mountType;
    private String ownerId;
    private String ownerDisplayName;
    private int unreadCommentsCount;
    private boolean hasPreview;
    private String note;
    private ShareeUser[] sharees;
    private String richWorkspace;
    private boolean isLocked;
    private FileLockType lockType;
    private String lockOwner;
    private String lockOwnerDisplayName;
    private long lockTimestamp;
    private String lockOwnerEditor;
    private long lockTimeout;
    private String lockToken;

    public RemoteFile() {
        resetData();
    }

    /**
     * Create new {@link RemoteFile} with given path.
     *
     * The path received must be URL-decoded. Path separator must be OCFile.PATH_SEPARATOR, and it must be the first character in 'path'.
     *
     * @param path The remote path of the file.
     */
    public RemoteFile(String path) {
        resetData();
        if (path == null || path.length() <= 0 || !path.startsWith(FileUtils.PATH_SEPARATOR)) {
            throw new IllegalArgumentException("Trying to create a OCFile with a non valid remote path: " + path);
        }
        remotePath = path;
    }

    public RemoteFile(WebdavEntry we) {
        this(we.decodedPath());
        setCreationTimestamp(we.getCreateTimestamp());
        setLength(we.getContentLength());
        setMimeType(we.getContentType());
        setModifiedTimestamp(we.getModifiedTimestamp());
        setUploadTimestamp(we.getUploadTimestamp());
        setEtag(we.getETag());
        setPermissions(we.getPermissions());
        setLocalId(we.getLocalId());
        setRemoteId(we.getRemoteId());
        setSize(we.getSize());
        setFavorite(we.isFavorite());
        setEncrypted(we.isEncrypted());
        setMountType(we.getMountType());
        setOwnerId(we.getOwnerId());
        setOwnerDisplayName(we.getOwnerDisplayName());
        setNote(we.getNote());
        setUnreadCommentsCount(we.getUnreadCommentsCount());
        setHasPreview(we.isHasPreview());
        setSharees(we.getSharees());
        setRichWorkspace(we.getRichWorkspace());
        setLocked(we.isLocked());
        setLockType(we.getLockOwnerType());
        setLockOwner(we.getLockOwnerId());
        setLockOwnerDisplayName(we.getLockOwnerDisplayName());
        setLockOwnerEditor(we.getLockOwnerEditor());
        setLockTimestamp(we.getLockTimestamp());
        setLockTimeout(we.getLockTimeout());
        setLockToken(we.getLockToken());
    }

    /**
     * Used internally. Reset all file properties
     */
    private void resetData() {
        remotePath = null;
        mimeType = null;
        length = 0;
        creationTimestamp = 0;
        modifiedTimestamp = 0;
        etag = null;
        permissions = null;
        localId = -1;
        remoteId = null;
        size = 0;
        favorite = false;
        encrypted = false;
        ownerId = "";
        ownerDisplayName = "";
        note = "";
        isLocked = false;
        lockOwner = null;
        lockType = null;
        lockOwnerDisplayName = null;
        lockOwnerEditor = null;
        lockTimestamp = 0;
        lockTimeout = 0;
        lockToken = null;
    }

    /**
     * Parcelable Methods
     */
    public static final Parcelable.Creator<RemoteFile> CREATOR
            = new Parcelable.Creator<RemoteFile>() {
        @Override
        public RemoteFile createFromParcel(Parcel source) {
            return new RemoteFile(source);
        }

        @Override
        public RemoteFile[] newArray(int size) {
            return new RemoteFile[size];
        }
    };


    /**
     * Reconstruct from parcel
     *
     * @param source The source parcel
     */
    protected RemoteFile(Parcel source) {
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        remotePath = source.readString();
        mimeType = source.readString();
        length = source.readLong();
        creationTimestamp = source.readLong();
        modifiedTimestamp = source.readLong();
        etag = source.readString();
        permissions = source.readString();
        localId = source.readLong();
        remoteId = source.readString();
        size = source.readLong();
        favorite = Boolean.parseBoolean(source.readString());
        encrypted = Boolean.parseBoolean(source.readString());
        mountType = (WebdavEntry.MountType) source.readSerializable();
        ownerId = source.readString();
        ownerDisplayName = source.readString();
        hasPreview = Boolean.parseBoolean(source.readString());
        note = source.readString();
        source.readParcelableArray(ShareeUser.class.getClassLoader());
        isLocked = source.readInt() == 1;
        lockType = FileLockType.fromValue(source.readInt());
        lockOwner = source.readString();
        lockOwnerDisplayName = source.readString();
        lockOwnerEditor = source.readString();
        lockTimestamp = source.readLong();
        lockTimeout = source.readLong();
        lockToken = source.readString();
    }

    @Override
    public int describeContents() {
        return this.hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(remotePath);
        dest.writeString(mimeType);
        dest.writeLong(length);
        dest.writeLong(creationTimestamp);
        dest.writeLong(modifiedTimestamp);
        dest.writeString(etag);
        dest.writeString(permissions);
        dest.writeLong(localId);
        dest.writeString(remoteId);
        dest.writeLong(size);
        dest.writeString(Boolean.toString(favorite));
        dest.writeString(Boolean.toString(encrypted));
        dest.writeSerializable(mountType);
        dest.writeString(ownerId);
        dest.writeString(ownerDisplayName);
        dest.writeString(Boolean.toString(hasPreview));
        dest.writeString(note);
        dest.writeParcelableArray(sharees, 0);
        dest.writeInt(isLocked ? 1 : 0);
        dest.writeInt(lockType != null ? lockType.getValue() : -1);
        dest.writeString(lockOwner);
        dest.writeString(lockOwnerDisplayName);
        dest.writeString(lockOwnerEditor);
        dest.writeLong(lockTimestamp);
        dest.writeLong(lockTimeout);
        dest.writeString(lockToken);
    }
}
