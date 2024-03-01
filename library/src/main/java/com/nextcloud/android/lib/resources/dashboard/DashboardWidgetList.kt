/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022-2024 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.dashboard

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DashboardWidgetList(
    val widgets: Map<String, DashboardWidget> = HashMap()
) : Parcelable
