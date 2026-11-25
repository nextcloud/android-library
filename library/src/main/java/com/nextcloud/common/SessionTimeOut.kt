/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.common

import com.nextcloud.common.SessionTimeOut.Companion.DEFAULT_CONNECTION_TIME_OUT
import com.nextcloud.common.SessionTimeOut.Companion.DEFAULT_READ_TIME_OUT

data class SessionTimeOut(
    val readTimeOut: Int,
    val connectionTimeOut: Int
) {
    companion object {
        const val DEFAULT_READ_TIME_OUT = 60_000
        const val DEFAULT_CONNECTION_TIME_OUT = 15_000
    }
}

val defaultSessionTimeOut = SessionTimeOut(DEFAULT_READ_TIME_OUT, DEFAULT_CONNECTION_TIME_OUT)
