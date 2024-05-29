/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.accounts

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

class ExternalLinksOperationIT : AbstractIT() {
    @Test
    fun retrieveExternalLinks() {
        val result = ExternalLinksOperation().execute(nextcloudClient)

        // check if external sites app (external) is installed
        assumeTrue(result.code != RemoteOperationResult.ResultCode.NOT_AVAILABLE)

        assertTrue(result.isSuccess)

        val data = result.resultData
        assertEquals(2, data?.size)

        assertEquals("Nextcloud", data?.get(0)?.name)
        assertEquals("https://www.nextcloud.com", data?.get(0)?.url)

        assertEquals("Forum", data?.get(1)?.name)
        assertEquals("https://help.nextcloud.com", data?.get(1)?.url)
    }
}
