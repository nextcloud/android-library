/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
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

    fun fileId(): String? = attributes["fileId"]

    fun remotePath(): String = attributes["path"] ?: parseRemotePath()

    private fun parseRemotePath(): String {
        val sanitizer =
            UrlQuerySanitizer().apply {
                allowUnregisteredParamaters = true
                unregisteredParameterValueSanitizer = UrlQuerySanitizer.getAllButNulLegal()
            }

        sanitizer.parseUrl(resourceUrl)

        val dirParam = sanitizer.getValue(PARAM_DIR)
        val dir =
            when (dirParam) {
                DIR_ROOT -> ""
                else -> dirParam
            }

        val file = sanitizer.getValue(PARAM_FILE)

        return "$dir/$file"
    }
}
