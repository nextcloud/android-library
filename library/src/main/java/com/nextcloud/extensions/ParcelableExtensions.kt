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

@Suppress("DEPRECATION")
inline fun <reified T> Parcel.readParcelableArrayBridge(type: Class<T>): Any? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.readParcelableArray(type::class.java.classLoader, this::class.java)
    } else {
        this.readParcelableArray(type::class.java.classLoader)
    }
}