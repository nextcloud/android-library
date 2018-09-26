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

package com.owncloud.android.lib.resources.shares;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.files.FileUtils;

import java.io.Serializable;


/**
 * Contains the data of a Share from the Share API
 * 
 * @author masensio
 *
 */
public class OCShare implements Parcelable, Serializable {

    /**
     * Generated - should be refreshed every time the class changes!!
     */
    private static final long serialVersionUID = 4124975224281327921L;

    private static final String TAG = OCShare.class.getSimpleName();

    public static final int DEFAULT_PERMISSION = -1;
    public static final int READ_PERMISSION_FLAG = 1;
    public static final int UPDATE_PERMISSION_FLAG = 2;
    public static final int CREATE_PERMISSION_FLAG = 4;
    public static final int DELETE_PERMISSION_FLAG = 8;
    public static final int SHARE_PERMISSION_FLAG = 16;
    public static final int MAXIMUM_PERMISSIONS_FOR_FILE =
        READ_PERMISSION_FLAG +
        UPDATE_PERMISSION_FLAG +
        SHARE_PERMISSION_FLAG;

    public static final int MAXIMUM_PERMISSIONS_FOR_FOLDER =
        MAXIMUM_PERMISSIONS_FOR_FILE +
        CREATE_PERMISSION_FLAG +
        DELETE_PERMISSION_FLAG;

    public static final int FEDERATED_PERMISSIONS_FOR_FILE_AFTER_OC9 =
        READ_PERMISSION_FLAG +
        UPDATE_PERMISSION_FLAG +
        CREATE_PERMISSION_FLAG +
        DELETE_PERMISSION_FLAG;

    public static final int FEDERATED_PERMISSIONS_FOR_FOLDER_UP_TO_OC9 =
        READ_PERMISSION_FLAG +
        UPDATE_PERMISSION_FLAG +
        CREATE_PERMISSION_FLAG +
        DELETE_PERMISSION_FLAG;

    public static final int FEDERATED_PERMISSIONS_FOR_FOLDER_AFTER_OC9 =
        FEDERATED_PERMISSIONS_FOR_FOLDER_UP_TO_OC9 +
        SHARE_PERMISSION_FLAG;

    private long id;
    private long fileSource;
    private long itemSource;
    private ShareType shareType;
    private String shareWith;
    private String path;
    private int permissions;
    private long sharedDate;
    private long expirationDate;
    private String token;
    private String sharedWithDisplayName;
    private boolean isFolder;
    private long userId;
    private long remoteId;
    private String shareLink;
    private boolean isPasswordProtected;
    private String note;
    
    public OCShare() {
    	super();
    	resetData();
    }
    
	public OCShare(String path) {
		resetData();
        if (path == null || path.length() <= 0 || !path.startsWith(FileUtils.PATH_SEPARATOR)) {
            Log_OC.e(TAG, "Trying to create a OCShare with a non valid path");
            throw new IllegalArgumentException("Trying to create a OCShare with a non valid path: " + path);
        }
        this.path = path;
	}

	/**
     * Used internally. Reset all file properties
     */
    private void resetData() {
        id = -1;
        fileSource = 0;
        itemSource = 0;
        shareType = ShareType.NO_SHARED;
        shareWith = "";
        path = "";
        permissions = -1;
        sharedDate = 0;
        expirationDate = 0;
        token = "";
        sharedWithDisplayName = "";
        isFolder = false;
        userId = -1;
        remoteId = -1;
        shareLink = "";
        isPasswordProtected = false;
        note = "";
    }	
    
    /// Getters and Setters
    
    public long getId() {
        return id;
    }
    
    public void setId(long id){
        this.id = id;
    }
    
    public long getFileSource() {
        return fileSource;
    }

    public void setFileSource(long fileSource) {
        this.fileSource = fileSource;
    }

    public long getItemSource() {
        return itemSource;
    }

    public void setItemSource(long itemSource) {
        this.itemSource = itemSource;
    }

    public ShareType getShareType() {
        return shareType;
    }

    public void setShareType(ShareType shareType) {
        this.shareType = shareType;
    }

    public String getShareWith() {
        return shareWith;
    }

    public void setShareWith(String shareWith) {
        this.shareWith = (shareWith != null) ? shareWith : "";
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = (path != null) ? path : "";
    }

    public int getPermissions() {
        return permissions;
    }

    public void setPermissions(int permissions) {
        this.permissions = permissions;
    }

    public long getSharedDate() {
        return sharedDate;
    }

    public void setSharedDate(long sharedDate) {
        this.sharedDate = sharedDate;
    }

    public long getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(long expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = (token != null) ? token : "";
    }

    public String getSharedWithDisplayName() {
        return sharedWithDisplayName;
    }

    public void setSharedWithDisplayName(String sharedWithDisplayName) {
        this.sharedWithDisplayName = (sharedWithDisplayName != null) ? sharedWithDisplayName : "";
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setIsFolder(boolean isFolder) {
        this.isFolder = isFolder;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getRemoteId() {
        return remoteId;
    }

    public void setIdRemoteShared(long remoteId) {
        this.remoteId = remoteId;
    }
    
    public String getShareLink() {
        return this.shareLink;
    }
    
    public void setShareLink(String shareLink) {
        this.shareLink = (shareLink != null) ? shareLink : "";
    }

    public void setIsPasswordProtected(boolean isPasswordProtected) {
        this.isPasswordProtected = isPasswordProtected;
    }

    public boolean isPasswordProtected() {
        if (!ShareType.PUBLIC_LINK.equals(shareType)) {
            return isPasswordProtected;
        } else {
            return shareWith.length() > 0;
        }
    }

    public void setNote(@NonNull String note) {
        this.note = note;
    }

    public String getNote() {
        return this.note;
    }
    
    /** 
     * Parcelable Methods
     */
    public static final Parcelable.Creator<OCShare> CREATOR = new Parcelable.Creator<OCShare>() {
        @Override
        public OCShare createFromParcel(Parcel source) {
            return new OCShare(source);
        }

        @Override
        public OCShare[] newArray(int size) {
            return new OCShare[size];
        }
    };
    
    /**
     * Reconstruct from parcel
     * 
     * @param source The source parcel
     */    
    protected OCShare(Parcel source) {
    	readFromParcel(source);
    }
    
    public void readFromParcel(Parcel source) {
        id = source.readLong();

        fileSource = source.readLong();
        itemSource = source.readLong();
        try {
            shareType = ShareType.valueOf(source.readString());
        } catch (IllegalArgumentException x) {
            shareType = ShareType.NO_SHARED;
        }
        shareWith = source.readString();
        path = source.readString();
        permissions = source.readInt();
        sharedDate = source.readLong();
        expirationDate = source.readLong();
        token = source.readString();
        sharedWithDisplayName = source.readString();
        isFolder = source.readInt() == 0;
        userId = source.readLong();
        remoteId = source.readLong();
        shareLink = source.readString();
        isPasswordProtected = source.readInt() == 1;
    }


	@Override
	public int describeContents() {
		return this.hashCode();
	}
	
	
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(fileSource);
        dest.writeLong(itemSource);
        dest.writeString((shareType == null) ? "" : shareType.name());
        dest.writeString(shareWith);
        dest.writeString(path);
        dest.writeInt(permissions);
        dest.writeLong(sharedDate);
        dest.writeLong(expirationDate);
        dest.writeString(token);
        dest.writeString(sharedWithDisplayName);
        dest.writeInt(isFolder ? 1 : 0);
        dest.writeLong(userId);
        dest.writeLong(remoteId);
        dest.writeString(shareLink);
        dest.writeInt(isPasswordProtected ? 1 : 0);
    }
}
