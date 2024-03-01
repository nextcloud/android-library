/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022-2024 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.dashboard

import com.google.gson.annotations.SerializedName

enum class DashBoardButtonType {
    @SerializedName("new")
    NEW,

    @SerializedName("more")
    MORE,

    @SerializedName("setup")
    SETUP
}
