/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2020 Tobias Kaminsky
 * Copyright (C) 2020 Nextcloud GmbH
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.owncloud.android.lib.resources.status;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

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
