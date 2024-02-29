/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.test

object RandomStringGenerator {
    private const val DEFAULT_LENGTH = 8
    private val ALLOWED_CHARACTERS = ('A'..'Z') + ('a'..'z') + ('0'..'9')

    @JvmOverloads
    @JvmStatic
    fun make(length: Int = DEFAULT_LENGTH): String {
        return (1..length)
            .map { ALLOWED_CHARACTERS.random() }
            .joinToString("")
    }
}
