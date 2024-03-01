/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.users

import android.os.Parcel
import android.os.Parcelable

class Status(val status: StatusType, val message: String?, val icon: String, val clearAt: Long) :
    Parcelable {
    constructor(parcel: Parcel) : this(
        StatusType.valueOf(parcel.readString().orEmpty()),
        parcel.readString(),
        parcel.readString().orEmpty(),
        parcel.readLong()
    )

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int
    ) {
        parcel.writeString(status.name)
        parcel.writeString(message)
        parcel.writeString(icon)
        parcel.writeLong(clearAt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Status> {
        override fun createFromParcel(parcel: Parcel): Status {
            return Status(parcel)
        }

        override fun newArray(size: Int): Array<Status?> {
            return arrayOfNulls(size)
        }
    }
}
