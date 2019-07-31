/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2019 Tobias Kaminsky
 *   Copyright (C) 2019 Nextcloud GmbH
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

import androidx.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ShareeUser implements Parcelable {
    @Getter private String userId;
    @Getter private String displayName;
    @Getter private ShareType shareType;


    public static final Creator<ShareeUser> CREATOR = new Creator<ShareeUser>() {
        @Override
        public ShareeUser createFromParcel(Parcel in) {
            return new ShareeUser(in);
        }

        @Override
        public ShareeUser[] newArray(int size) {
            return new ShareeUser[size];
        }
    };

    protected ShareeUser(Parcel in) {
        userId = in.readString();
        displayName = in.readString();
        shareType = ShareType.fromValue(in.readInt());
    }

    @Override
    public int describeContents() {
        return this.hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(displayName);
        dest.writeInt(shareType.getValue());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof ShareeUser)) {
            return false;
        }
        ShareeUser that = (ShareeUser) obj;

        return this.userId.equals(that.userId) &&
                this.displayName.equals(that.displayName) &&
                this.shareType == that.shareType;
    }
}
