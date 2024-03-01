/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020-2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.status;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CapabilityBooleanTypeTest {

    @Test
    public void test() {
        assertTrue(CapabilityBooleanType.fromBooleanValue(true).isTrue());
        assertTrue(CapabilityBooleanType.fromBooleanValue(false).isFalse());

        assertTrue(CapabilityBooleanType.fromValue(-2).isUnknown());
        assertTrue(CapabilityBooleanType.fromValue(-1).isUnknown());
        assertTrue(CapabilityBooleanType.fromValue(-0).isFalse());
        assertTrue(CapabilityBooleanType.fromValue(1).isTrue());
        assertTrue(CapabilityBooleanType.fromValue(2).isUnknown());

        assertTrue(CapabilityBooleanType.valueOf("UNKNOWN").isUnknown());
        assertTrue(CapabilityBooleanType.valueOf("FALSE").isFalse());
        assertTrue(CapabilityBooleanType.valueOf("TRUE").isTrue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException() {
        CapabilityBooleanType.valueOf("wrongValue");
    }

}
