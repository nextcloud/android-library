/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 ZetaTom <70907959+ZetaTom@users.noreply.github.com>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.extensions

import android.os.Build
import android.os.Parcel

@Suppress("DEPRECATION", "UNCHECKED_CAST")
inline fun <reified T> Parcel.readParcelableArrayCompat(type: Class<T>): Array<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.readParcelableArray(type::class.java.classLoader, this::class.java)
    } else {
        this.readParcelableArray(type::class.java.classLoader)
    } as? Array<T>
}

@Suppress("DEPRECATION")
inline fun <reified T> Parcel.readSerializableCompat(type: Class<T>): T {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.readSerializable(type::class.java.classLoader, this::class.java) as T
    } else {
        this.readSerializable() as T
    }
}
