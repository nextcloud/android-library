/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.governance

import com.nextcloud.android.lib.resources.governance.model.SensitivityLabelInfo
import com.nextcloud.android.lib.resources.governance.model.SensitivityLabelScope
import com.owncloud.android.lib.ocs.OcsKotlinResponse
import com.owncloud.android.lib.ocs.ocsJson
import org.junit.Assert.assertEquals
import org.junit.Test

class SensitivityLabelInfoParsingTest {
    @Test
    fun parsesAvailableSensitivityLabelsResponse() {
        val json =
            """
            {
              "ocs": {
                "meta": { "status": "ok", "statuscode": 200, "message": "OK" },
                "data": [
                  {
                    "id": "confidential",
                    "name": "Confidential",
                    "priority": 30,
                    "description": "Restricted to a small group",
                    "color": "#ff0000",
                    "scopes": ["FILES", "MAILS"]
                  },
                  {
                    "id": "public",
                    "name": "Public",
                    "priority": 10,
                    "description": "Anyone may access",
                    "color": "#00ff00",
                    "scopes": ["FILES"]
                  }
                ]
              }
            }
            """.trimIndent()

        val labels =
            ocsJson
                .decodeFromString<OcsKotlinResponse<List<SensitivityLabelInfo>>>(json)
                .ocs
                .data

        assertEquals(2, labels.size)

        val first = labels[0]
        assertEquals("confidential", first.id)
        assertEquals("Confidential", first.name)
        assertEquals(30L, first.priority)
        assertEquals("Restricted to a small group", first.description)
        assertEquals("#ff0000", first.color)
        assertEquals(listOf(SensitivityLabelScope.FILES, SensitivityLabelScope.MAILS), first.scopes)

        assertEquals(listOf(SensitivityLabelScope.FILES), labels[1].scopes)
    }
}
