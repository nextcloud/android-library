/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022-2024 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.dashboard

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DashboardWidget(
    val id: String,
    val title: String,
    val order: Int,
    @SerializedName("icon_url") val iconUrl: String,
    @SerializedName("item_icons_round") val roundIcons: Boolean,
    val buttons: List<DashboardButton>?
) : Parcelable
