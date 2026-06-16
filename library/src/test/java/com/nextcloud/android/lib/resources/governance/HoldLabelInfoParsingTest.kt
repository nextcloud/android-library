/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.governance

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.owncloud.android.lib.ocs.ServerResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class HoldLabelInfoParsingTest {
    @Test
    fun parsesAvailableHoldLabelsResponse() {
        val json =
            """
            {
              "ocs": {
                "meta": { "status": "ok", "statuscode": 200, "message": "OK" },
                "data": [
                  {
                    "id": "litigation-hold",
                    "name": "Litigation hold",
                    "priority": 30,
                    "description": "Preserve for legal proceedings",
                    "color": "#ff0000",
                    "scopes": ["FILES", "MAILS"]
                  },
                  {
                    "id": "no-hold",
                    "name": "No hold",
                    "priority": 10,
                    "description": "No legal hold applied",
                    "color": "#00ff00",
                    "scopes": ["FILES"]
                  }
                ]
              }
            }
            """.trimIndent()

        val element = JsonParser.parseString(json)
        val type = object : TypeToken<ServerResponse<List<HoldLabelInfo>>>() {}.type
        val response: ServerResponse<List<HoldLabelInfo>> = Gson().fromJson(element, type)

        val labels = response.ocs?.data
        assertNotNull(labels)
        assertEquals(2, labels!!.size)

        val first = labels[0]
        assertEquals("litigation-hold", first.id)
        assertEquals("Litigation hold", first.name)
        assertEquals(30L, first.priority)
        assertEquals("Preserve for legal proceedings", first.description)
        assertEquals("#ff0000", first.color)
        assertEquals(listOf(LabelScope.FILES, LabelScope.MAILS), first.scopes)

        assertEquals(listOf(LabelScope.FILES), labels[1].scopes)
    }
}
