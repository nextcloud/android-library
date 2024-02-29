/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common

/**
 * Search result of an unified search
 */
data class SearchResult(
    var name: String = "",
    var isPaginated: Boolean = false,
    var entries: List<SearchResultEntry> = emptyList(),
    var cursor: String? = ""
)
