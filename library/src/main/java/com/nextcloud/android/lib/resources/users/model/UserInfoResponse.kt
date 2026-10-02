/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Your Name <your@email.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.nextcloud.android.lib.resources.users.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInfoResponse(
    val id: String? = null,
    val enabled: Boolean? = null,
    @SerialName("display-name")
    val displayName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val website: String? = null,
    val twitter: String? = null,
    val quota: QuotaResponse? = null,
    val groups: List<String>? = null
)
