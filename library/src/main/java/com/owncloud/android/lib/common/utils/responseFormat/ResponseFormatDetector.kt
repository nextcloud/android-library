/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.common.utils.responseFormat

import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

object ResponseFormatDetector {
    fun detectFormat(input: String): ResponseFormat =
        when {
            isXML(input) -> ResponseFormat.XML
            isJSON(input) -> ResponseFormat.JSON
            else -> ResponseFormat.UNKNOWN
        }

    private fun isJSON(input: String): Boolean =
        isJsonObject(input) || isJsonArray(input)

    fun isJsonObject(input: String): Boolean = runCatching { JSONObject(input) }.isSuccess

    fun isJsonArray(input: String): Boolean = runCatching { JSONArray(input) }.isSuccess

    @Suppress("TooGenericExceptionCaught")
    private fun isXML(input: String): Boolean =
        try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val stream = ByteArrayInputStream(input.toByteArray())
            builder.parse(stream)
            true
        } catch (_: Exception) {
            false
        }
}
