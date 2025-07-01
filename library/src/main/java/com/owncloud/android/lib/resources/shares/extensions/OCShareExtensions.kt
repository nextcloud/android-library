/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.shares.extensions

import com.owncloud.android.lib.resources.shares.OCShare
import org.json.JSONArray
import org.json.JSONObject

private const val KEY = "key"
private const val SCOPE_KEY = "scope"
private const val DOWNLOAD_KEY = "download"
private const val PERMISSIONS_KEY = "permissions"
private const val VALUE_KEY = "value"
private const val ENABLED_KEY = "enabled"

fun toggleAllowDownloadAndSync(
    attributes: String?,
    isChecked: Boolean,
    useV2DownloadAttributes: Boolean
): String? {
    var jsonArray = JSONArray()
    if (!attributes.isNullOrEmpty()) {
        jsonArray = JSONArray(attributes)
    }

    val downloadAttr = jsonArray.findDownloadAttribute()
    val enabledKey = getEnabledKey(useV2DownloadAttributes)

    if (downloadAttr != null) {
        downloadAttr.put(enabledKey, isChecked)
    } else {
        jsonArray.put(
            JSONObject().apply {
                put(KEY, DOWNLOAD_KEY)
                put(SCOPE_KEY, PERMISSIONS_KEY)
                put(enabledKey, isChecked)
            }
        )
    }

    return jsonArray.toString()
}

@Suppress("ReturnCount")
fun OCShare?.isAllowDownloadAndSyncEnabled(useV2DownloadAttributes: Boolean): Boolean {
    if (this?.attributes.isNullOrEmpty()) return false

    val jsonArray = JSONArray(this.attributes)
    val downloadAttr = jsonArray.findDownloadAttribute() ?: return false
    val enabledKey = getEnabledKey(useV2DownloadAttributes)

    return downloadAttr.optBoolean(enabledKey, false)
}

private fun JSONArray.findDownloadAttribute(): JSONObject? =
    (0 until length())
        .asSequence()
        .map { getJSONObject(it) }
        .find {
            it.optString(KEY) == DOWNLOAD_KEY &&
                it.optString(SCOPE_KEY) == PERMISSIONS_KEY
        }

private fun getEnabledKey(isV2: Boolean): String = if (isV2) VALUE_KEY else ENABLED_KEY
