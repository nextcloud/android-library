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

class GovernanceLabelResponseParsingTest {
    @Test
    fun parsesGenericLabelResponse() {
        val json =
            """
            {
              "ocs": {
                "meta": { "status": "ok", "statuscode": 200, "message": "OK" },
                "data": { "message": "Label applied" }
              }
            }
            """.trimIndent()

        val element = JsonParser.parseString(json)
        val type = object : TypeToken<ServerResponse<GovernanceLabelResponse>>() {}.type
        val response: ServerResponse<GovernanceLabelResponse> = Gson().fromJson(element, type)

        val data = response.ocs?.data
        assertNotNull(data)
        assertEquals("Label applied", data!!.message)
    }
}
