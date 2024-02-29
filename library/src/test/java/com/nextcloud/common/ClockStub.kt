/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro.brey@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.common

import com.nextcloud.android.lib.core.Clock

class ClockStub(private val currentTimeValue: Long) : Clock {
    override val currentTimeMillis: Long
        get() = currentTimeValue
}
