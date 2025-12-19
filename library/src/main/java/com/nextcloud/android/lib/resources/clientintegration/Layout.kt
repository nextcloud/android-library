/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2025 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.clientintegration

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Layout(
    @SerializedName("orientation")
    var orientation: LayoutOrientation,
    var layoutRows: List<LayoutRow>
) : Parcelable
