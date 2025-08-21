/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Your Name <your@email.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.declarativeui

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Endpoint(
    val name: String,
    val url: String,
    @SerializedName("android_icon")
    val icon: String?,
    val filter: String?
) : Parcelable
