/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android

import com.owncloud.android.lib.common.utils.responseFormat.ResponseFormat
import com.owncloud.android.lib.common.utils.responseFormat.ResponseFormatDetector
import junit.framework.TestCase.assertEquals
import org.junit.Test

class ResponseFormatDetectorTests {

    @Test
    fun testJsonDetection() {
        val json = """{ "name": "Alice", "age": 30 }"""
        assertEquals(ResponseFormat.JSON, ResponseFormatDetector.detectFormat(json))
    }

    @Test
    fun testJsonArrayDetection() {
        val jsonArray = """[{"name": "Alice"}, {"name": "Bob"}]"""
        assertEquals(ResponseFormat.JSON, ResponseFormatDetector.detectFormat(jsonArray))
    }

    @Test
    fun testXmlDetection() {
        val xml = """<person><name>Alice</name><age>30</age></person>"""
        assertEquals(ResponseFormat.XML, ResponseFormatDetector.detectFormat(xml))
    }

    @Test
    fun testInvalidFormat() {
        val invalid = "Just a plain string"
        assertEquals(ResponseFormat.UNKNOWN, ResponseFormatDetector.detectFormat(invalid))
    }

    @Test
    fun testEmptyString() {
        val empty = ""
        assertEquals(ResponseFormat.UNKNOWN, ResponseFormatDetector.detectFormat(empty))
    }
}
