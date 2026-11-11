/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.assistant

import kotlinx.serialization.json.Json

internal val assistantRequestJson =
    Json {
        encodeDefaults = true
        explicitNulls = false
    }
