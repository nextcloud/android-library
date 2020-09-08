/* ownCloud Android Library is available under MIT license
 *   Copyright (C) 2015 ownCloud Inc.
 *   Copyright (C) 2012  Bartek Przybylski
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

package com.owncloud.android.lib.resources.status;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public class OwnCloudVersion implements Comparable<OwnCloudVersion>, Parcelable {
    public static final OwnCloudVersion nextcloud_13 = new OwnCloudVersion(0x0D000000); // 13.0
    public static final OwnCloudVersion nextcloud_14 = new OwnCloudVersion(0x0E000000); // 14.0
    public static final OwnCloudVersion nextcloud_15 = new OwnCloudVersion(0x0F000000); // 15.0
    public static final OwnCloudVersion nextcloud_16 = new OwnCloudVersion(0x10000000); // 16.0
    public static final OwnCloudVersion nextcloud_17 = new OwnCloudVersion(0x11000000); // 17.0
    public static final OwnCloudVersion nextcloud_18 = new OwnCloudVersion(0x12000000); // 18.0
    public static final OwnCloudVersion nextcloud_19 = new OwnCloudVersion(0x13000000); // 19.0
    public static final OwnCloudVersion nextcloud_20 = new OwnCloudVersion(0x14000000); // 20.0

    public static final int MINIMUM_VERSION_FOR_MEDIA_STREAMING = nextcloud_14.version; // 14.0
    public static final int MINIMUM_VERSION_FOR_NOTE_ON_SHARE = nextcloud_14.version; // 14.0
    
    private static final int MAX_DOTS = 3;

    // format is in version
    // 0xAABBCCDD
    // for version AA.BB.CC.DD
    // ie version 2.0.3 will be stored as 0x02000300
    private int version;
    @Getter private boolean versionValid;

    protected OwnCloudVersion(int version) {
        this.version = version;
        versionValid = true;
    }

    public OwnCloudVersion(String version) {
        this.version = 0;
        versionValid = false;
        int countDots = version.length() - version.replace(".", "").length();

        // Complete the version. Version must have 3 dots
        StringBuilder versionWithDots = new StringBuilder(version);
        for (int i = countDots; i < MAX_DOTS; i++) {
            versionWithDots.append(".0");
        }

        parseVersion(versionWithDots.toString());
    }

    @NonNull
    @Override
    public String toString() {
    	String versionToString = String.valueOf((version >> (8*MAX_DOTS)) % 256);
    	for (int i = MAX_DOTS - 1; i >= 0; i-- ) {
    		versionToString = versionToString + "." + String.valueOf((version >> (8*i)) % 256);
    	}
        return versionToString;
    }
    
    public String getVersion() {
    	return toString();
    }

    @Override
    public int compareTo(@NonNull OwnCloudVersion another) {
        return another.version == version ? 0 : another.version < version ? 1 : -1;
    }

    public boolean isNewerOrEqual(@NonNull OwnCloudVersion another) {
        return version >= another.version;
    }
    
    public boolean isSameMajorVersion(@NonNull OwnCloudVersion another) {
        return this.getMajorVersionNumber() == another.getMajorVersionNumber();
    }

    public int getMajorVersionNumber() {
        return version >> (8 * MAX_DOTS) % 256;
    }

    private void parseVersion(String version) {
    	try {
    		this.version = getParsedVersion(version);
            versionValid = true;
    		
    	} catch (Exception e) {
            versionValid = false;
        }
    }
    
    private int getParsedVersion(String version) throws NumberFormatException {
    	int versionValue = 0;

    	// get only numeric part 
    	version = version.replaceAll("[^\\d.]", "");

    	String[] nums = version.split("\\.");
    	for (int i = 0; i < nums.length && i <= MAX_DOTS; i++) {
    		versionValue += Integer.parseInt(nums[i]);
    		if (i < nums.length - 1) {
    			versionValue = versionValue << 8;
    		}
    	}

    	return versionValue; 
    }

    public boolean isMediaStreamingSupported() {
        return version >= MINIMUM_VERSION_FOR_MEDIA_STREAMING;
    }
    
    public boolean isNoteOnShareSupported() {
        return version >= MINIMUM_VERSION_FOR_NOTE_ON_SHARE;
    }

    public boolean isHideFileDownloadSupported() {
        return isNewerOrEqual(nextcloud_15);
    }

    public boolean isShareesOnDavSupported() {
        return isNewerOrEqual(nextcloud_17);
    }

    public boolean isRemoteWipeSupported() {
        return isNewerOrEqual(nextcloud_17);
    }

    /*
     * Autogenerated Parcelable
     */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(version);
        dest.writeByte(versionValid ? (byte) 1 : (byte) 0);
    }

    protected OwnCloudVersion(Parcel in) {
        version = in.readInt();
        versionValid = in.readByte() != 0;
    }

    public static final Creator<OwnCloudVersion> CREATOR = new Creator<OwnCloudVersion>() {
        @Override
        public OwnCloudVersion createFromParcel(Parcel source) {
            return new OwnCloudVersion(source);
        }

        @Override
        public OwnCloudVersion[] newArray(int size) {
            return new OwnCloudVersion[size];
        }
    };
}
