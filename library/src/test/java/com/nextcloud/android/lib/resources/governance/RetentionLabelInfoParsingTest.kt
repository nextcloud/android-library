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

class RetentionLabelInfoParsingTest {
    @Test
    fun parsesAvailableRetentionLabelsResponse() {
        val json =
            """
            {
              "ocs": {
                "meta": { "status": "ok", "statuscode": 200, "message": "OK" },
                "data": [
                  {
                    "id": "keep-10-years",
                    "name": "Keep 10 years",
                    "priority": 30,
                    "description": "Retain for ten years",
                    "color": "#ff0000",
                    "scopes": ["FILES", "MAILS"]
                  },
                  {
                    "id": "no-retention",
                    "name": "No retention",
                    "priority": 10,
                    "description": "May be deleted any time",
                    "color": "#00ff00",
                    "scopes": ["FILES"]
                  }
                ]
              }
            }
            """.trimIndent()

        val element = JsonParser.parseString(json)
        val type = object : TypeToken<ServerResponse<List<RetentionLabelInfo>>>() {}.type
        val response: ServerResponse<List<RetentionLabelInfo>> = Gson().fromJson(element, type)

        val labels = response.ocs?.data
        assertNotNull(labels)
        assertEquals(2, labels!!.size)

        val first = labels[0]
        assertEquals("keep-10-years", first.id)
        assertEquals("Keep 10 years", first.name)
        assertEquals(30L, first.priority)
        assertEquals("Retain for ten years", first.description)
        assertEquals("#ff0000", first.color)
        assertEquals(listOf(LabelScope.FILES, LabelScope.MAILS), first.scopes)

        assertEquals(listOf(LabelScope.FILES), labels[1].scopes)
    }
}
