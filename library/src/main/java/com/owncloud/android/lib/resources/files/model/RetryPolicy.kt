/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.files.model

data class RetryPolicy(
    val maxAttempts: Int = 3,
    val initialDelayMs: Long = 500L,
    val maxDelayMs: Long = 8_000L,
    val backoffMultiplier: Double = 2.0
) {
    init {
        require(maxAttempts >= 1) { "maxAttempts must be >= 1" }
        require(initialDelayMs >= 0) { "initialDelayMs must be >= 0" }
    }

    companion object {
        @JvmField
        val DEFAULT = RetryPolicy()

        @JvmField
        val NONE = RetryPolicy(maxAttempts = 1)
    }
}
