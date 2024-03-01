/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Unpublished <unpublished@users.noreply.github.com>
 * SPDX-FileCopyrightText: 2017 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Quota data model
 */
@Parcelize
data class ExternalLink(
    val id: Int,
    val iconUrl: String,
    val language: String,
    val type: ExternalLinkType,
    val name: String,
    val url: String,
    val redirect: Boolean
) : Parcelable
