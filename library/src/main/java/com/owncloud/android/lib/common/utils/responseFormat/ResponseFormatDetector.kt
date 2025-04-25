/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.common.utils.responseFormat

import com.owncloud.android.lib.common.utils.Log_OC
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

object ResponseFormatDetector {
    private const val TAG = "ResponseFormatDetector"
    private const val JSON_OBJECT_PREFIX = "{"
    private const val JSON_ARRAY_PREFIX = "["

    fun detectFormat(input: String): ResponseFormat {
        return when {
            isJson(input) -> ResponseFormat.JSON
            isXml(input) -> ResponseFormat.XML
            else -> ResponseFormat.UNKNOWN
        }
    }

    private fun isJson(input: String): Boolean {
        return try {
            val trimmed = input.trim()
            if (trimmed.startsWith(JSON_OBJECT_PREFIX)) {
                JSONObject(trimmed)
            } else if (trimmed.startsWith(JSON_ARRAY_PREFIX)) {
                JSONArray(trimmed)
            } else {
                return false
            }
            true
        } catch (e: Exception) {
            Log_OC.e(TAG, "Exception isJson: $e")
            false
        }
    }

    private fun isXml(input: String): Boolean {
        return try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val stream = ByteArrayInputStream(input.toByteArray())
            builder.parse(stream)
            true
        } catch (e: Exception) {
            Log_OC.e(TAG, "Exception isXml: $e")
            false
        }
    }
}
