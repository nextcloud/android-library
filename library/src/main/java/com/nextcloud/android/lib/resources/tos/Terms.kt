/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.tos

data class Terms(
    val terms: List<Term>,
    val hasSigned: Boolean,
    val languages: Map<String, String>
)
