/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025-2026 TSI-mc <surinder.kumar@t-systems.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.albums

import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.MultiStatusResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoAlbumEntryTest {
    @Test
    fun albumNameFromHrefWithTrailingSlash() {
        assertEquals("vacation2024", entryOf("/remote.php/dav/photos/user_id/albums/vacation2024/").albumName)
    }

    @Test
    fun albumNameFromHrefWithoutTrailingSlash() {
        assertEquals("vacation2024", entryOf("/remote.php/dav/photos/user_id/albums/vacation2024").albumName)
    }

    @Test
    fun albumNameFromNestedHref() {
        assertEquals("europe", entryOf("/remote.php/dav/photos/user_id/albums/travel/europe/").albumName)
    }

    @Test
    fun albumNameIsEmptyForHrefWithoutName() {
        assertEquals("", entryOf("/").albumName)
        assertEquals("", entryOf("///").albumName)
    }

    @Test
    fun albumNameFromHrefWithoutSeparator() {
        assertEquals("holiday", entryOf("holiday").albumName)
    }

    @Test
    fun albumWithoutPropertiesFallsBackToDefaults() {
        val entry = entryOf("/remote.php/dav/photos/user_id/albums/vacation2024/")

        assertEquals(0L, entry.lastPhoto)
        assertEquals(0, entry.nbItems)
        assertEquals(null, entry.location)
        assertTrue(entry.collaborators.isEmpty())
    }

    private fun entryOf(href: String): PhotoAlbumEntry =
        PhotoAlbumEntry("https://www.example.com", MultiStatusResponse(href, HttpStatus.SC_OK))
}
