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
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.ByteArrayInputStream

class WebdavEntryTest {
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

        private fun getElement(): Element {
            val document =
                DomUtil.parseDocument(
                    ByteArrayInputStream(EXAMPLE_MULTI_STATUS.toByteArray())
                )
            return document.documentElement
        }

        private fun getMultiStatus(): MultiStatus = MultiStatus.createFromXml(getElement())
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
