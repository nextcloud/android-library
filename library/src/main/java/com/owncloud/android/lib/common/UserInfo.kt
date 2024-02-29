/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Unpublished <unpublished@users.noreply.github.com>
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * User information data model
 */
@Parcelize
data class UserInfo(
    var id: String?,
    var enabled: Boolean?,
    @SerializedName(value = "display-name", alternate = ["displayname"])
    var displayName: String?,
    var email: String?,
    var phone: String?,
    var address: String?,
    @SerializedName(value = "website", alternate = ["webpage"])
    var website: String?,
    var twitter: String?,
    var quota: Quota?,
    var groups: ArrayList<String>?
) : Parcelable
