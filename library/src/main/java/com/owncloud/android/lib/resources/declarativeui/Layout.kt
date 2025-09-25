/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Your Name <your@email.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.declarativeui

import com.google.gson.annotations.SerializedName

data class Layout(
    @SerializedName("orientation")
    var orientation: Orientation,
    var rows: List<Row>
)
