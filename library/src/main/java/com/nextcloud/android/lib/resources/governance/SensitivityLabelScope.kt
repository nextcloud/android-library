/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.governance

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SensitivityLabelScope {
    @SerialName("FILES")
    FILES,

    @SerialName("MAILS")
    MAILS
}
