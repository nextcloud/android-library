/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2023 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.core

interface Clock {
    /**
     * Current epoch time in millis
     */
    val currentTimeMillis: Long
}
