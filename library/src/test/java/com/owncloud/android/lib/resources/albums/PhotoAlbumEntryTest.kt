/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 TSI-mc <surinder.kumar@t-systems.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.albums

import org.apache.jackrabbit.webdav.MultiStatusResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoAlbumEntryTest {
    @Test
    fun testAlbumName_withTrailingSlash() {
        val entry = createTestEntry("/remote.php/dav/photos/user_id/albums/vacation2024/")
        assertEquals("vacation2024", entry.albumName)
    }

    @Test
    fun testAlbumName_withoutTrailingSlash() {
        val entry = createTestEntry("/remote.php/dav/photos/user_id/albums/vacation2024")
        assertEquals("vacation2024", entry.albumName)
    }

    @Test
    fun testAlbumName_nestedPath() {
        val entry = createTestEntry("/remote.php/dav/photos/user_id/albums/travel/europe/")
        assertEquals("europe", entry.albumName)
    }

    @Test
    fun testAlbumName_singleSlash() {
        val entry = createTestEntry("/")
        assertEquals("", entry.albumName)
    }

    @Test
    fun testAlbumName_onlySlashes() {
        val entry = createTestEntry("///")
        assertEquals("", entry.albumName)
    }

    @Test
    fun testAlbumName_noSlash() {
        val entry = createTestEntry("holiday")
        assertEquals("holiday", entry.albumName)
    }

    // Helper method to create a stub entry
    private fun createTestEntry(href: String): PhotoAlbumEntry {
        val response = MultiStatusResponse(href, 200)
        return PhotoAlbumEntry(response)
    }
}
