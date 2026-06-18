/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.status

import org.junit.Assert.assertEquals
import org.junit.Test

class E2EVersionTests {
    @Test
    fun testFromValueWhenGiven1ShouldReturnTrue() {
        assertEquals(E2EVersion.V1_0, E2EVersion.fromValue("1"))
    }

    @Test
    fun testFromValueWhenGiven1Dot0ShouldReturnTrue() {
        assertEquals(E2EVersion.V1_0, E2EVersion.fromValue("1.0"))
    }

    @Test
    fun testFromValueWhenGiven1Dot1ShouldReturnTrue() {
        assertEquals(E2EVersion.V1_1, E2EVersion.fromValue("1.1"))
    }

    @Test
    fun testFromValueWhenGiven1Dot2ShouldReturnTrue() {
        assertEquals(E2EVersion.V1_2, E2EVersion.fromValue("1.2"))
    }

    @Test
    fun testFromValueWhenGiven2ShouldReturnTrue() {
        assertEquals(E2EVersion.V2_0, E2EVersion.fromValue("2"))
    }

    @Test
    fun testFromValueWhenGiven2Dot0ShouldReturnTrue() {
        assertEquals(E2EVersion.V2_0, E2EVersion.fromValue("2.0"))
    }

    @Test
    fun testFromValueWhenGiven2Dot1ShouldReturnTrue() {
        assertEquals(E2EVersion.V2_1, E2EVersion.fromValue("2.1"))
    }

    @Test
    fun testFromValueWhenGivenEmptyShouldReturnTrue() {
        assertEquals(E2EVersion.UNKNOWN, E2EVersion.fromValue(""))
    }

    @Test
    fun testFromValueWhenGivenUnknownShouldReturnTrue() {
        assertEquals(E2EVersion.UNKNOWN, E2EVersion.fromValue("3"))
    }

    @Test
    fun testFromValueWhenGivenNullShouldReturnTrue() {
        assertEquals(E2EVersion.UNKNOWN, E2EVersion.fromValue(null))
    }

    @Test
    fun testFromValueWhenGiven3Dot0ShouldReturnTrue() {
        val version = E2EVersion.fromValue("3.0")
        assertEquals(E2EVersion.UNKNOWN, version)
        assertEquals("3.0", version.unknownValue)
    }

    @Test
    fun testValues() {
        assertEquals(E2EVersion.V1_0.value, "1.0")
        assertEquals(E2EVersion.V1_1.value, "1.1")
        assertEquals(E2EVersion.V1_2.value, "1.2")
        assertEquals(E2EVersion.V2_0.value, "2.0")
        assertEquals(E2EVersion.V2_1.value, "2.1")
        assertEquals(E2EVersion.UNKNOWN.value, "")
    }
}
