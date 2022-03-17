/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2020 Tobias Kaminsky
 *   Copyright (C) 2020 Nextcloud GmbH
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
package com.owncloud.android.lib.common

import android.net.UrlQuerySanitizer

/**
 * One search result entry of an unified search
 */
data class SearchResultEntry(
    var thumbnailUrl: String = "",
    var title: String = "",
    var subline: String = "",
    var resourceUrl: String = "",
    var icon: String = "",
    var rounded: Boolean = false,
    var attributes: Map<String, String> = emptyMap()
) {

    companion object {
        private const val PARAM_DIR = "dir"
        private const val PARAM_FILE = "scrollto"
        private const val DIR_ROOT = "/"
    }

    val isFile: Boolean
        get() = fileId() != null || listOf(PARAM_DIR, PARAM_FILE).all { resourceUrl.contains(it) }

    fun fileId(): String? {
        return attributes["fileId"]
    }

    fun remotePath(): String {
        return attributes["path"] ?: parseRemotePath()
    }

    private fun parseRemotePath(): String {
        val sanitizer = UrlQuerySanitizer().apply {
            allowUnregisteredParamaters = true
            unregisteredParameterValueSanitizer = UrlQuerySanitizer.getAllButNulLegal()
        }

        sanitizer.parseUrl(resourceUrl)

        val dirParam = sanitizer.getValue(PARAM_DIR)
        val dir = when (dirParam) {
            DIR_ROOT -> ""
            else -> dirParam
        }

        val file = sanitizer.getValue(PARAM_FILE)

        return "$dir/$file"
    }
}
