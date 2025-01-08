/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import java.io.IOException

class CreateLocalFileException(
    val path: String,
    cause: Throwable
) : Exception(cause) {
    override val message: String = "File could not be created"

    /**
     * Checks if the path associated to the exception contains invalid characters.
     * There is no better way to check this, as `Paths` is not available in API < 26, and since this lib has a very low
     * minSdk, that can't even be worked around with an `if` block.
     */
    fun isCausedByInvalidPath(): Boolean =
        cause is IOException &&
            (
                path.isEmpty() ||
                    INVALID_CHARS.any {
                        path.contains(it)
                    }
            )

    companion object {
        private const val INVALID_CHARS = "\\:*?\"<>|"
    }
}
