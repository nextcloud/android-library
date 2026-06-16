/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.governance

data class EntityLabels(
    val sensitivity: List<SensitivityLabelInfo>,
    val retention: List<RetentionLabelInfo>,
    val hold: List<HoldLabelInfo>
)
