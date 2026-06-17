/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.governance

import com.google.gson.annotations.SerializedName

enum class SensitivityLabelScope {
    @SerializedName("FILES")
    FILES,

    @SerializedName("MAILS")
    MAILS
}
