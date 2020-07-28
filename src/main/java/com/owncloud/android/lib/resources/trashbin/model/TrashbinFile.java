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

package com.owncloud.android.lib.resources.trashbin.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.owncloud.android.lib.common.network.WebdavEntry;
import com.owncloud.android.lib.resources.files.FileUtils;
import com.owncloud.android.lib.resources.files.model.ServerFileInterface;

import java.io.Serializable;

import androidx.annotation.VisibleForTesting;
import lombok.Getter;
import lombok.Setter;

/**
 * Contains the data of a Trashbin File from a WebDavEntry.
 *
 * @author Tobias Kaminsky
 */
@Getter
@Setter
public class TrashbinFile implements Parcelable, Serializable, ServerFileInterface {
    /**
     * Generated - should be refreshed every time the class changes!!
     */
    private static final long serialVersionUID = -432910968238077774L;

    public static final String DIRECTORY = "DIR";

    private String fullRemotePath;
    private String remotePath;
    private String mimeType;
    private long fileLength;
    private String remoteId;
    private String fileName;
    private String originalLocation;
    private long deletionTimestamp;

    /**
     * For trashbin this is the same as remoteId
     */
    @Override
    public String getLocalId() {
        return getRemoteId();
    }

    @Override
    public boolean isFolder() {
        return DIRECTORY.equals(mimeType);
    }

    public boolean isHidden() {
        return getFileName().startsWith(".");
    }

    @Override
    public boolean isFavorite() {
        return false;
    }

    public TrashbinFile(WebdavEntry we, String userId) {
        String path = we.decodedPath();
        
        if (path == null || path.length() <= 0 || !path.startsWith(FileUtils.PATH_SEPARATOR)) {
            throw new IllegalArgumentException("Trying to create a TrashbinFile with a non valid remote path: " + path);
        }
        
        fullRemotePath = path;
        remotePath = fullRemotePath.replace("/trashbin/" + userId + "/trash", "");
        
        setMimeType(we.getContentType());

        if (isFolder()) {
            setFileLength(we.getSize());
        } else {
            setFileLength(we.getContentLength());
        }

        setFileName(we.getTrashbinFilename());
        setOriginalLocation(we.getTrashbinOriginalLocation());
        setDeletionTimestamp(we.getTrashbinDeletionTimestamp());
        setRemoteId(we.getRemoteId());
    }

    @VisibleForTesting
    public TrashbinFile(String fileName,
                        String mimeType,
                        String remotePath,
                        String originalLocation,
                        long deletionTimestamp,
                        long fileLength) {
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.remotePath = remotePath;
        this.originalLocation = originalLocation;
        this.deletionTimestamp = deletionTimestamp;
        this.fileLength = fileLength;
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
        fileLength = source.readLong();
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
        dest.writeLong(fileLength);
        dest.writeString(remoteId);
    }
}
