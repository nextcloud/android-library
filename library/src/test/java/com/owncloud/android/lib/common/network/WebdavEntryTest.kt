/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro.brey@nextcloud.com>
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.network

import com.owncloud.android.lib.resources.files.model.FileLockType
import org.apache.jackrabbit.webdav.MultiStatus
import org.apache.jackrabbit.webdav.xml.DomUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.ByteArrayInputStream

class WebdavEntryTest {
    @Test
    fun testPercentEncodingHexCaseInBasePath() {
        val upperBase = "/remote.php/dav/files/%E6%B5%8B%E8%AF%95/"
        val lowerBase = "/remote.php/dav/files/%e6%b5%8b%e8%af%95/"
        val mixedBase = "/remote.php/dav/files/%e6%B5%8b%E8%aF%95/"

        for (base in listOf(upperBase, lowerBase, mixedBase)) {
            for (hrefBase in listOf(upperBase, lowerBase, mixedBase)) {
                assertEquals("", entryWithHref(hrefBase, base).path)
                assertEquals("Photo.JPG", entryWithHref(hrefBase + "Photo.JPG", base).path)
            }
        }
    }

    @Test
    fun testLiteralBasePathWithRegexMetacharacters() {
        val base = "/remote.php/dav/files/test[1]+(work)/"
        assertEquals("test.md", entryWithHref(base + "test.md", base).path)
    }

    @Test
    fun testPreservesEncodedRelativePath() {
        val base = "/remote.php/dav/files/%E6%B5%8B/"
        val relativePath = "Photo%2fName%25+%e6%b5%8b.JPG"
        assertEquals(relativePath, entryWithHref(base.lowercase() + relativePath, base).path)
    }

    @Test
    fun testAbsoluteHref() {
        val base = "/remote.php/dav/files/%E6%B5%8B/"
        assertEquals("test.md", entryWithHref("https://example.org" + base.lowercase() + "test.md", base).path)
    }

    @Test
    fun testRootWithoutTrailingBaseSeparator() {
        val base = "/remote.php/dav/files/%E6%B5%8B"
        assertEquals("/", entryWithHref(base.lowercase() + "/", base).path)
    }

    @Test
    fun testLegacyWebdavAndUploadPaths() {
        assertEquals("test.md", entryWithHref("/remote.php/webdav/test.md", "/remote.php/webdav/").path)
        assertEquals(
            "uploads/test/test.md",
            entryWithHref("/remote.php/dav/uploads/test//test.md", "/remote.php/dav/").path
        )
    }

    @Test
    fun testRejectsDifferentPathCase() {
        assertThrows(IllegalArgumentException::class.java) {
            entryWithHref("/remote.php/dav/files/Test/test.md", "/remote.php/dav/files/test/")
        }
    }

    @Test
    fun testDoesNotDecodeEncodedSeparators() {
        assertThrows(IllegalArgumentException::class.java) {
            entryWithHref("/remote.php/dav/files/test%2Fother/test.md", "/remote.php/dav/files/test/other/")
        }
        assertThrows(IllegalArgumentException::class.java) {
            entryWithHref("/remote.php/dav/files/test%252f/test.md", "/remote.php/dav/files/test%252F/")
        }
    }

    @Test
    fun testRejectsEmptyBasePath() {
        assertThrows(IllegalArgumentException::class.java) {
            entryWithHref("/remote.php/dav/files/test/test.md", "")
        }
    }

    @Test
    fun testRejectsRegexWildcardMatch() {
        assertThrows(IllegalArgumentException::class.java) {
            entryWithHref("/remoteXphp/dav/files/test/test.md", "/remote.php/dav/files/test/")
        }
    }

    private fun entryWithHref(
        href: String,
        base: String
    ): WebdavEntry = WebdavEntry(getMultiStatus(href).responses[0], base)

    companion object {
        private const val EXAMPLE_MULTI_STATUS = """<?xml version="1.0"?>
        <d:multistatus xmlns:d="DAV:" xmlns:nc="http://nextcloud.org/ns" xmlns:oc="http://owncloud.org/ns">
            <d:response>
                <d:href>/remote.php/dav/files/test/test.md</d:href>
                <d:propstat>
                    <d:prop>
                        <d:getlastmodified>Fri, 07 Jan 2022 14:42:34 GMT</d:getlastmodified>
                        <d:getetag>&quot;6262bbe303188&quot;</d:getetag>
                        <d:getcontenttype>text/markdown</d:getcontenttype>
                        <d:resourcetype />
                        <oc:fileid>171</oc:fileid>
                        <oc:permissions>RGDNVW</oc:permissions>
                        <oc:size>7</oc:size>
                        <d:getcontentlength>7</d:getcontentlength>
                        <nc:has-preview>true</nc:has-preview>
                        <nc:mount-type></nc:mount-type>
                        <x1:share-permissions xmlns:x1="http://open-collaboration-services.org/ns">
19
</x1:share-permissions>
                        <oc:tags />
                        <oc:favorite>0</oc:favorite>
                        <oc:owner-id>test</oc:owner-id>
                        <oc:owner-display-name>test</oc:owner-display-name>
                        <oc:share-types />
                        <oc:comments-unread>0</oc:comments-unread>
                        <nc:lock>1</nc:lock>
                        <nc:lock-owner>test</nc:lock-owner>
                        <nc:lock-owner-displayname>test</nc:lock-owner-displayname>
                        <nc:lock-time>1650637795</nc:lock-time>
                        <nc:lock-timeout>1800</nc:lock-timeout>
                        <nc:lock-owner-type>0</nc:lock-owner-type>
                        <nc:encrypted>0</nc:encrypted>
                    </d:prop>
                    <d:status>HTTP/1.1 200 OK</d:status>
                </d:propstat>
                <d:propstat>
                    <d:prop>
                        <d:quota-available-bytes />
                        <nc:is-encrypted />
                        <nc:lock-owner-editor />
                        <nc:rich-workspace-file />
                    </d:prop>
                    <d:status>HTTP/1.1 404 Not Found</d:status>
                </d:propstat>
            </d:response>
        </d:multistatus>
        """

        private fun getElement(href: String): Element {
            val xml = EXAMPLE_MULTI_STATUS.replace("/remote.php/dav/files/test/test.md", href)
            val document =
                DomUtil.parseDocument(
                    ByteArrayInputStream(xml.toByteArray())
                )
            return document.documentElement
        }

        private fun getMultiStatus(href: String = "/remote.php/dav/files/test/test.md"): MultiStatus =
            MultiStatus.createFromXml(getElement(href))
    }

    @Test
    fun testParseLockProps() {
        val entry = WebdavEntry(getMultiStatus().responses[0], "/remote.php/dav/files/test/")
        assertTrue("Entry not locked", entry.isLocked)
        assertEquals("Wrong lock type", FileLockType.MANUAL, entry.lockOwnerType)
        assertEquals("Wrong lock owner", "test", entry.lockOwnerId)
        assertEquals("Wrong lock owner display name", "test", entry.lockOwnerDisplayName)
        assertEquals("Wrong lock owner editor", null, entry.lockOwnerEditor)
        assertEquals("Wrong lock timestamp", 1650637795, entry.lockTimestamp)
        assertEquals("Wrong lock timeout", 1800, entry.lockTimeout)
        assertEquals("Wrong lock token", null, entry.lockToken)
        assertEquals("Wrong encryption state", false, entry.isEncrypted)
    }
}
