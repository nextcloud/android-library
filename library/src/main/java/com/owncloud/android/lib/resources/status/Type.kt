/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Your Name <your@email.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.status

import com.google.gson.annotations.SerializedName

enum class Type(val string: String) {
    @SerializedName("context-menu")
    CONTEXT_MENU("context-menu"),

    @SerializedName("create-new")
    CREATE_NEW("create-new")
}
