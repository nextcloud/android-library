/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Your Name <your@email.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.status

import com.google.gson.annotations.SerializedName
import com.owncloud.android.lib.resources.declarativeui.Endpoint

data class Hook(val type: Type, val endpoints: List<Endpoint>)

enum class Type {
    @SerializedName("context-menu")
    CONTEXT_MENU,

    @SerializedName("create-new")
    CREATE_NEW
}
