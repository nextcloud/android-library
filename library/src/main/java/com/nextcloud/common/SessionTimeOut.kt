/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.common

data class SessionTimeOut(val readTimeOut: Int, val connectionTimeOut: Int)

val defaultSessionTimeOut = SessionTimeOut(40000, 5000)
