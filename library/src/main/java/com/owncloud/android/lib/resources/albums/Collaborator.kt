/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.albums

import com.owncloud.android.lib.resources.shares.ShareType

data class Collaborator(
    val id: String,
    val label: String,
    val type: ShareType,
    val shareLink: String
)
