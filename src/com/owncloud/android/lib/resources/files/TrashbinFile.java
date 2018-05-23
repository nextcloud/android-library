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

package com.owncloud.android.lib.resources.files;

import android.os.Parcel;
import android.os.Parcelable;

import com.owncloud.android.lib.common.network.WebdavEntry;

import java.io.Serializable;

/**
 * Contains the data of a Trashbin File from a WebDavEntry.
 *
 * @author Tobias Kaminsky
 */
public class TrashbinFile implements Parcelable, Serializable, ServerFileInterface {

    /**
     * Generated - should be refreshed every time the class changes!!
     */
    private static final long serialVersionUID = 3130865437811248452L;

    public static final String DIRECTORY = "DIR";

    private String fullRemotePath;
    private String remotePath;
    private String mimeType;
    private long length;
    private String remoteId;
    private String fileName;
    private String originalLocation;
    private long deletionTimestamp;

    /**
     * Getters and Setters.
     */

    public String getFullRemotePath() {
        return fullRemotePath;
    }

    @Override
    public boolean getIsFavorite() {
        return false;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setFullRemotePath(String fullRemotePath) {
        this.fullRemotePath = fullRemotePath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getFileLength() {
        return length;
    }

    public void setFileLength(long length) {
        this.length = length;
    }

    public String getRemoteId() {
        return remoteId;
    }
    
    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

    /**
     * For trashbin this is the same as remoteId
     */
    public String getLocalId() {
        return getRemoteId();
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setOriginalLocation(String originalLocation) {
        this.originalLocation = originalLocation;
    }
    
    public String getOriginalLocation() {
        return originalLocation;
    }
    
    public void setDeletionTimestamp(long deletionTimestamp) {
        this.deletionTimestamp = deletionTimestamp;
    }
    
    public long getDeletionTimestamp() {
        return deletionTimestamp;
    }
    
    public boolean isFolder() {
        return mimeType != null && mimeType.equals(DIRECTORY);
    }

    public boolean isHidden() {
        return getFileName().startsWith(".");
    }

    public TrashbinFile(WebdavEntry we, String userId) {
        String path = we.decodedPath();
        
        if (path == null || path.length() <= 0 || !path.startsWith(FileUtils.PATH_SEPARATOR)) {
            throw new IllegalArgumentException("Trying to create a TrashbinFile with a non valid remote path: " + path);
        }
        
        fullRemotePath = path;
        remotePath = fullRemotePath.replace("/trashbin/"+userId+"/trash", "");
        
        setMimeType(we.contentType());

        if (isFolder()) {
            setFileLength(we.size());
        } else {
            setFileLength(we.contentLength());
        }
        
        setFileName(we.getTrashbinFilename());
        setOriginalLocation(we.getTrashbinOriginalLocation());
        setDeletionTimestamp(we.getTrashbinDeletionTimestamp());
        setRemoteId(we.remoteId());
        
    }

    /**
     * Parcelable Methods
     */
    public static final Creator<TrashbinFile> CREATOR = new Creator<TrashbinFile>() {
        @Override
        public TrashbinFile createFromParcel(Parcel source) {
            return new TrashbinFile(source);
        }

        @Override
        public TrashbinFile[] newArray(int size) {
            return new TrashbinFile[size];
        }
    };


    /**
     * Reconstruct from parcel
     *
     * @param source The source parcel
     */
    protected TrashbinFile(Parcel source) {
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        fullRemotePath = source.readString();
        mimeType = source.readString();
        length = source.readLong();
        remoteId = source.readString();
    }

    @Override
    public int describeContents() {
        return this.hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fullRemotePath);
        dest.writeString(mimeType);
        dest.writeLong(length);
        dest.writeString(remoteId);
    }
}
