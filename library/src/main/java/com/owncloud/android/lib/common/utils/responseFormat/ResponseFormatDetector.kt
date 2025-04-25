/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.common.utils.responseFormat

import com.owncloud.android.lib.common.utils.Log_OC
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

object ResponseFormatDetector {
    private const val TAG = "ResponseFormatDetector"

    fun detectFormat(input: String): ResponseFormat =
        when {
            isJSON(input) -> ResponseFormat.JSON
            isXML(input) -> ResponseFormat.XML
            else -> ResponseFormat.UNKNOWN
        }

    private fun isJSON(input: String): Boolean =
        try {
            JSONObject(input)
            true
        } catch (e: JSONException) {
            try {
                Log_OC.i(TAG, "Info it's not JSONObject: $e")
                JSONArray(input)
                true
            } catch (e: JSONException) {
                Log_OC.e(TAG, "Exception it's not JSONArray: $e")
                false
            }
        }

    @Suppress("TooGenericExceptionCaught")
    private fun isXML(input: String): Boolean =
        try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val stream = ByteArrayInputStream(input.toByteArray())
            builder.parse(stream)
            true
        } catch (e: Exception) {
            Log_OC.e(TAG, "Exception isXML: $e")
            false
        }
}
