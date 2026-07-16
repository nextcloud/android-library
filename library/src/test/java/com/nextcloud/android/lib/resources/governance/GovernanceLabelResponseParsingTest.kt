/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.android.lib.resources.governance

import kotlinx.serialization.decodeFromString
import com.nextcloud.android.lib.resources.governance.model.GovernanceLabelResponse
import com.owncloud.android.lib.ocs.OcsResponse
import com.owncloud.android.lib.ocs.ocsJson
import org.junit.Assert.assertEquals
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

        val data =
            ocsJson.decodeFromString<OcsResponse<GovernanceLabelResponse>>(json).ocs.data

        assertEquals("Label applied", data.message)
    }
}
