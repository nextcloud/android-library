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

import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.files.FileUtils;

import java.io.Serializable;

import androidx.annotation.NonNull;
import lombok.Getter;
import lombok.Setter;


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

    @Getter @Setter private long id;
    @Getter @Setter private long fileSource;
    @Getter @Setter private long itemSource;
    @Getter @Setter private ShareType shareType;
    @Getter private String shareWith;
    @Getter private String path;
    @Getter @Setter private int permissions;
    @Getter @Setter private long sharedDate;
    @Getter @Setter private long expirationDate;
    @Getter private String token;
    @Getter private String sharedWithDisplayName;
    @Getter @Setter private boolean folder;
    @Getter @Setter private String userId;
    @Getter @Setter private long remoteId;
    @Getter private String shareLink;
    @Setter private boolean isPasswordProtected;
    @Getter private String note;
    @Getter @Setter private boolean hideFileDownload;
    @Getter @Setter private String label;
    
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
        folder = false;
        userId = "";
        remoteId = -1;
        shareLink = "";
        isPasswordProtected = false;
        note = "";
        hideFileDownload = false;
        label = "";
    }	
    
    // custom Getters and Setters
    public void setShareWith(String shareWith) {
        this.shareWith = (shareWith != null) ? shareWith : "";
    }

    public void setPath(String path) {
        this.path = (path != null) ? path : "";
    }

    public void setToken(String token) {
        this.token = (token != null) ? token : "";
    }

    public void setSharedWithDisplayName(String sharedWithDisplayName) {
        this.sharedWithDisplayName = (sharedWithDisplayName != null) ? sharedWithDisplayName : "";
    }

    public void setShareLink(String shareLink) {
        this.shareLink = (shareLink != null) ? shareLink : "";
    }

    public boolean isPasswordProtected() {
        if (ShareType.PUBLIC_LINK == shareType) {
            return shareWith.length() > 0;
        } else {
            return isPasswordProtected;
        }
    }

    public void setNote(@NonNull String note) {
        this.note = note;
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
        folder = source.readInt() == 0;
        userId = source.readString();
        remoteId = source.readLong();
        shareLink = source.readString();
        isPasswordProtected = source.readInt() == 1;
        hideFileDownload = source.readInt() == 1;
        label = source.readString();
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
        dest.writeInt(folder ? 1 : 0);
        dest.writeString(userId);
        dest.writeLong(remoteId);
        dest.writeString(shareLink);
        dest.writeInt(isPasswordProtected ? 1 : 0);
        dest.writeInt(hideFileDownload ? 1 : 0);
        dest.writeString(label);
    }
}
