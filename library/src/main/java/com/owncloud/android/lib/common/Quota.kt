/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Unpublished <unpublished@users.noreply.github.com>
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Quota data model
 */

@Parcelize
data class Quota(
    val free: Long = 0,
    val used: Long = 0,
    val total: Long = 0,
    val relative: Double = 0.0,
    val quota: Long = 0
) : Parcelable {
    constructor(quota: Long) : this(0, quota = quota)
}
