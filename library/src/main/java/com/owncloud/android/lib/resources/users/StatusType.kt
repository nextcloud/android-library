/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.users

import com.google.gson.annotations.SerializedName

enum class StatusType(
    val string: String
) {
    @SerializedName("online")
    ONLINE("online"),

    @SerializedName("offline")
    OFFLINE("offline"),

    @SerializedName("dnd")
    DND("dnd"),

    @SerializedName("away")
    AWAY("away"),

    @SerializedName("invisible")
    INVISIBLE("invisible")
}
