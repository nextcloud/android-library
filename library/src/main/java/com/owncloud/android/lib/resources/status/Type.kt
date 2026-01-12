/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2025 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.status

import com.google.gson.annotations.SerializedName

enum class Type(
    val string: String
) {
    @SerializedName("context-menu")
    CONTEXT_MENU("context-menu"),

    @SerializedName("create-new")
    CREATE_NEW("create-new")
}
