/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Your Name <your@email.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.declarativeui

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Endpoint(
    val name: String,
    val relativeURL: String
) : Parcelable
