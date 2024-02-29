/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021-2024 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.profile

import java.util.ArrayList

/**
 * HoverCard data model
 */
data class HoverCard(
    val userId: String,
    val displayName: String,
    val actions: List<Action> = ArrayList()
)
