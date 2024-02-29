/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro@alvarobrey.com>
 * SPDX-FileCopyrightText: 2022 Unpublished <unpublished@users.noreply.github.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * List of templates data model
 */
@Parcelize
data class TemplateList(
    val templates: Map<String, Template> = HashMap()
) : Parcelable
