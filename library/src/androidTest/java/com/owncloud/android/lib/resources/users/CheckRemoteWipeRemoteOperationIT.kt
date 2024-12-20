/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.users

import android.text.TextUtils
import com.nextcloud.android.lib.resources.users.GenerateAppPasswordRemoteOperation
import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.common.OwnCloudBasicCredentials
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckRemoteWipeRemoteOperationIT : AbstractIT() {
    @Test
    fun testCheckWipe() {
        val appTokenResult = GenerateAppPasswordRemoteOperation().execute(client)
        assertTrue(appTokenResult.isSuccess)

        val appPassword = appTokenResult.resultData
        assertFalse(TextUtils.isEmpty(appPassword))

        client.credentials =
            OwnCloudBasicCredentials(
                client.credentials.username,
                appPassword,
                true
            )

        val wipeResult = CheckRemoteWipeRemoteOperation().execute(client)

        // device should not be wiped
        assertFalse(wipeResult.isSuccess)
    }
}
