/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Your Name <your@email.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.nextcloud.android.lib.resources.users.model

import kotlinx.serialization.Serializable

@Serializable
data class QuotaResponse(
    val free: Long = 0,
    val used: Long = 0,
    val total: Long = 0,
    val relative: Double = 0.0,
    val quota: Long = 0
)
