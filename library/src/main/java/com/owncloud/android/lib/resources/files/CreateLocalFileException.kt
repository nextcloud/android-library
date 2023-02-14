/* Nextcloud Android Library is available under MIT license
 *
 *   @author Álvaro Brey
 *   Copyright (C) 2023 Álvaro Brey
 *   Copyright (C) 2023 Nextcloud GmbH
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 */

package com.owncloud.android.lib.resources.files

import java.io.IOException

class CreateLocalFileException(val path: String, cause: Throwable) : Exception(cause) {

    override val message: String = "File could not be created"

    /**
     * Checks if the path associated to the exception contains invalid characters.
     * There is no better way to check this, as `Paths` is not available in API < 26, and since this lib has a very low
     * minSdk, that can't even be worked around with an `if` block.
     */
    fun isCausedByInvalidPath(): Boolean {
        return cause is IOException && (path.isEmpty() || INVALID_CHARS.any { path.contains(it) })
    }

    companion object {
        private const val INVALID_CHARS = "\\:*?\"<>|"
    }
}
