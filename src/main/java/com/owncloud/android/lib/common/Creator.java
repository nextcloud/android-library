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

package com.owncloud.android.lib.common;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Creator of direct editing data model
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Creator implements Parcelable {
    public String id;
    public String editor;
    public String name;
    public String extension;
    public String mimetype;
    public boolean templates;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(editor);
        dest.writeString(name);
        dest.writeString(extension);
        dest.writeString(mimetype);
        dest.writeInt(templates ? 1 : 0);
    }
    
    private Creator(Parcel read) {
        id = read.readString();
        editor = read.readString();
        name = read.readString();
        extension = read.readString();
        mimetype = read.readString();
        templates = read.readInt() != 0;
    }

    public static final Creator<com.owncloud.android.lib.common.Creator> CREATOR =
            new Creator<com.owncloud.android.lib.common.Creator>() {

                @Override
                public com.owncloud.android.lib.common.Creator createFromParcel(android.os.Parcel source) {
                    return new com.owncloud.android.lib.common.Creator(source);
                }

                @Override
                public com.owncloud.android.lib.common.Creator[] newArray(int size) {
                    return new com.owncloud.android.lib.common.Creator[size];
                }
            };
}
