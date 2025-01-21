/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 ZetaTom <70907959+zetatom@users.noreply.github.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.extensions

import android.os.Build
import android.os.Parcel
import java.io.Serializable

inline fun <reified T : Serializable?> Parcel.readSerializableCompat(): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        readSerializable(T::class.java.classLoader, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        readSerializable() as? T
    }
