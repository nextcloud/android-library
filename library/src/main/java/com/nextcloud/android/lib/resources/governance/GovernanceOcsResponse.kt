/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.governance

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Strict [Json] used to decode governance responses.
 *
 * Unknown keys (e.g. the OCS `meta` wrapper or additive server fields) are ignored, but required
 * fields are enforced: a missing or type-mismatched field fails the decode immediately (fail fast)
 * instead of silently producing a half-populated object.
 */
internal val governanceJson = Json { ignoreUnknownKeys = true }

@Serializable
internal data class GovernanceOcsResponse<T>(
    val ocs: GovernanceOcs<T>
)

@Serializable
internal data class GovernanceOcs<T>(
    val data: T
)
