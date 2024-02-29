/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Chris Narkiewicz <hello@ezaquarii.com>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.common

import android.accounts.Account

interface User {
    val accountName: String

    @Deprecated("Temporary workaround")
    fun toPlatformAccount(): Account
}
