/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Your Name <your@email.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.nextcloud.android.lib.resources.users

import com.nextcloud.android.lib.resources.users.model.UserInfoResponse
import com.owncloud.android.lib.ocs.OcsKotlinResponse
import com.owncloud.android.lib.ocs.ocsJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserInfoResponseParsingTest {
    @Test
    fun parsesUserInfoResponse() {
        val json =
            """
            {
              "ocs": {
                "meta": { "status": "ok", "statuscode": 200, "message": "OK" },
                "data": {
                  "id": "user1",
                  "enabled": true,
                  "display-name": "User One",
                  "email": "user1@example.com",
                  "quota": {
                    "free": 100,
                    "used": 50,
                    "total": 150,
                    "relative": 33.3,
                    "quota": -3
                  },
                  "groups": ["admin", "users"]
                }
              }
            }
            """.trimIndent()

        val userInfo =
            ocsJson
                .decodeFromString<OcsKotlinResponse<UserInfoResponse>>(json)
                .ocs
                .data

        assertEquals("user1", userInfo.id)
        assertEquals("User One", userInfo.displayName)
        assertEquals("user1@example.com", userInfo.email)
        assertEquals(-3L, userInfo.quota?.quota)
        assertEquals(listOf("admin", "users"), userInfo.groups)
        assertNull(userInfo.phone)
    }
}
