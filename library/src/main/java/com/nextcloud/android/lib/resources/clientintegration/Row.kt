/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Your Name <your@email.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.clientintegration

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Row(
    val children: List<Element>
) : Parcelable
