/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.governance

data class RetentionLabelInfo(
    val id: String,
    val name: String,
    val priority: Long,
    val description: String,
    val color: String,
    val scopes: List<LabelScope>
)
