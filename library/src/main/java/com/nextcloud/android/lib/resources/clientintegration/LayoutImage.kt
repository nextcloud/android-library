/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.clientintegration

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LayoutImage(
    val url: String
) : Element,
    Parcelable
