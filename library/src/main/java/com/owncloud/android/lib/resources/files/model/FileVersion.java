/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2018 Tobias Kaminsky
 *   Copyright (C) 2018 Nextcloud GmbH
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

import lombok.Getter;
import lombok.Setter;


/**
 * Contains the data of a versioned file from a WebDavEntry.
 */
public class FileVersion implements Parcelable, ServerFileInterface {

    /**
     * Generated - should be refreshed every time the class changes!!
     */
    private static final long serialVersionUID = 5276021208979796734L;

    public static final String DIRECTORY = "DIR";

    @Getter @Setter private String mimeType;
    @Getter @Setter private long fileLength;
    @Getter @Setter private long modifiedTimestamp;
    @Getter private String remoteId;

    @Override
    public boolean isFavorite() {
        return false;
    }

    @Override
    public String getFileName() {
        return String.valueOf(modifiedTimestamp / 1000);
    }

    @Override
    public String getRemotePath() {
        return "";
    }

    /**
     * For file version this is the same as remoteId
     */
    @Override
    public String getLocalId() {
        return getRemoteId();
    }

    public boolean isFolder() {
        return DIRECTORY.equals(mimeType);
    }

    public boolean isHidden() {
        return getFileName().startsWith(".");
    }

    public FileVersion(String fileId, WebdavEntry we) {
        remoteId = fileId;
        setMimeType(we.getContentType());

        if (isFolder()) {
            setFileLength(we.getSize());
        } else {
            setFileLength(we.getContentLength());
        }

        setModifiedTimestamp(we.getModifiedTimestamp());
    }

    /**
     * Parcelable Methods
     */
    public static final Creator<FileVersion> CREATOR = new Creator<FileVersion>() {
        @Override
        public FileVersion createFromParcel(Parcel source) {
            return new FileVersion(source);
        }

        @Override
        public FileVersion[] newArray(int size) {
            return new FileVersion[size];
        }
    };


    /**
     * Reconstruct from parcel
     *
     * @param source The source parcel
     */
    protected FileVersion(Parcel source) {
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        mimeType = source.readString();
        fileLength = source.readLong();
    }

    @Override
    public int describeContents() {
        return this.hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mimeType);
        dest.writeLong(fileLength);
    }
}
