/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Unpublished <unpublished@users.noreply.github.com>
 * SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Direct editing data model
 */
@Parcelize
data class DirectEditing(
    val editors: Map<String, Editor> = HashMap(),
    val creators: Map<String, Creator> = HashMap()
) : Parcelable
