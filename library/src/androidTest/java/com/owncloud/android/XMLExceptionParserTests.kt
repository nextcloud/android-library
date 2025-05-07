/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android

import com.owncloud.android.lib.common.operations.XMLExceptionParser
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayInputStream

class XMLExceptionParserTests {
    @Test
    fun testVirusException() {
        val virusException =
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<d:error xmlns:d=\"DAV:\" xmlns:s=\"http://sabredav.org/ns\">\n" +
                "  <s:exception>OCA\\DAV\\Connector\\Sabre\\Exception\\UnsupportedMediaType" +
                "</s:exception>\n" +
                "  <s:message>Virus Eicar-Test-Signature is detected in the file. " +
                "Upload cannot be completed.</s:message>\n" +
                "</d:error>"

        val inputStream = ByteArrayInputStream(virusException.toByteArray())
        val xmlParser = XMLExceptionParser(inputStream)

        Assert.assertTrue(xmlParser.isVirusException)
        Assert.assertFalse(xmlParser.isInvalidCharacterException)
    }

    @Test
    fun testInvalidCharacterException() {
        val virusException =
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<d:error xmlns:d=\"DAV:\" xmlns:s=\"http://sabredav.org/ns\">\n" +
                "  <s:exception>OC\\Connector\\Sabre\\Exception\\InvalidPath</s:exception>\n" +
                "  <s:message>Wrong Path</s:message>\n" +
                "</d:error>"

        val inputStream = ByteArrayInputStream(virusException.toByteArray())
        val xmlParser = XMLExceptionParser(inputStream)

        Assert.assertTrue(xmlParser.isInvalidCharacterException)
        Assert.assertFalse(xmlParser.isVirusException)
    }

    @Test
    fun testInvalidCharacterUploadException() {
        val virusException =
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<d:error xmlns:d=\"DAV:\" xmlns:s=\"http://sabredav.org/ns\">\n" +
                "  <s:exception>OCP\\Files\\InvalidPathException</s:exception>\n" +
                "  <s:message>Wrong Path</s:message>\n" +
                "</d:error>"

        val inputStream = ByteArrayInputStream(virusException.toByteArray())
        val xmlParser = XMLExceptionParser(inputStream)

        Assert.assertTrue(xmlParser.isInvalidCharacterException)
        Assert.assertFalse(xmlParser.isVirusException)
    }

    @Test
    fun testEmptyString() {
        val emptyString = ""

        val inputStream = ByteArrayInputStream(emptyString.toByteArray())
        val xmlParser = XMLExceptionParser(inputStream)

        Assert.assertFalse(xmlParser.isVirusException)
        Assert.assertFalse(xmlParser.isInvalidCharacterException)
    }

    @Test
    fun testString() {
        val emptyString = ""

        val inputStream = ByteArrayInputStream(emptyString.toByteArray())
        val xmlParser = XMLExceptionParser(inputStream)

        Assert.assertFalse(xmlParser.isVirusException)
        Assert.assertFalse(xmlParser.isInvalidCharacterException)
    }
}
