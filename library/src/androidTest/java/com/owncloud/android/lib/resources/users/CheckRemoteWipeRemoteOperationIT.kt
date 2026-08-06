/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2020-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2025 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.resources.users

import android.text.TextUtils
import com.nextcloud.android.lib.resources.users.GenerateAppPasswordRemoteOperation
import com.owncloud.android.AbstractIT
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class CheckRemoteWipeRemoteOperationIT : AbstractIT() {
    @Test
    fun testCheckWipe() {
        val appTokenResult = GenerateAppPasswordRemoteOperation().execute(client)
        assertTrue(appTokenResult.isSuccess)

        val appPassword = appTokenResult.resultData
        assertFalse(TextUtils.isEmpty(appPassword))

        val wipeResult = CheckRemoteWipeRemoteOperation(appPassword).execute(nextcloudClient)

        // device should not be wiped
        assertFalse(wipeResult.isSuccess)
    }
}
