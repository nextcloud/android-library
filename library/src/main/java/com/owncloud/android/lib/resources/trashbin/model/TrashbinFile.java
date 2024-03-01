/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2018-2022 Tobias Kaminsky <tobias@kaminsky.me>
 *
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.trashbin.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.VisibleForTesting;

import com.owncloud.android.lib.common.network.WebdavEntry;
import com.owncloud.android.lib.resources.files.FileUtils;
import com.owncloud.android.lib.resources.files.model.ServerFileInterface;

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
    public long getLocalId() {
        return Long.parseLong(getRemoteId());
    }

    @Override
    public boolean isFolder() {
        return DIRECTORY.equals(mimeType);
    }

    public boolean isHidden() {
        return getFileName().startsWith(".");
    }

    @Override
    public long getFileLength() {
        return fileLength;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public String getRemotePath() {
        return remotePath;
    }

    @Override
    public String getRemoteId() {
        return String.valueOf(remoteId);
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

    public String getFullRemotePath() {
        return this.fullRemotePath;
    }

    public String getOriginalLocation() {
        return this.originalLocation;
    }

    public long getDeletionTimestamp() {
        return this.deletionTimestamp;
    }

    public void setFullRemotePath(String fullRemotePath) {
        this.fullRemotePath = fullRemotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setOriginalLocation(String originalLocation) {
        this.originalLocation = originalLocation;
    }

    public void setDeletionTimestamp(long deletionTimestamp) {
        this.deletionTimestamp = deletionTimestamp;
    }
}
