/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.users

import android.text.TextUtils
import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.status.NextcloudVersion
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class GenerateOneTimeAppPasswordRemoteOperationIT : AbstractIT() {
    @Test
    fun generateAppPassword() {
        // only on NC33+
        testOnlyOnServer(NextcloudVersion.nextcloud_33)

        val sut = GenerateOneTimeAppPasswordRemoteOperation()
        val result = sut.execute(nextcloudClient)

        assertTrue(result.isSuccess)

        val appPassword = result.getResultData()
        assertFalse(TextUtils.isEmpty(appPassword))

        // re-using onetime password should fail
        assertFalse(GenerateOneTimeAppPasswordRemoteOperation().execute(nextcloudClient).isSuccess)
    }
}
