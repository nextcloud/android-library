/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.governance

import com.nextcloud.android.lib.resources.governance.model.LabelType
import org.junit.Assert.assertEquals
import org.junit.Test

class LabelTypeTest {
    @Test
    fun pathValuesMatchSpecEnum() {
        assertEquals("SENSITIVITY", LabelType.SENSITIVITY.value)
        assertEquals("RETENTION", LabelType.RETENTION.value)
        assertEquals("HOLD", LabelType.HOLD.value)
    }
}
