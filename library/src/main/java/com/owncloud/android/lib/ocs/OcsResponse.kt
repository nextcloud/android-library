/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.ocs

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

const val SEPARATOR = "/"

/**
 * Strict [Json] used to decode OCS responses with kotlinx.serialization.
 *
 * Unknown keys (e.g. the OCS `meta` wrapper or additive server fields) are ignored, but required
 * fields are enforced: a missing or type-mismatched field fails the decode immediately (fail fast)
 * instead of silently producing a half-populated object.
 */
internal val ocsJson = Json { ignoreUnknownKeys = true }

/**
 * Serializable counterpart of [ServerResponse]: the outer `{ "ocs": { "data": ... } }` envelope
 * shared by every OCS endpoint. Reusable for any endpoint whose payload type [T] is `@Serializable`.
 */
@Serializable
internal data class OcsResponse<T>(
    val ocs: Ocs<T>
)

@Serializable
internal data class Ocs<T>(
    val data: T
)
