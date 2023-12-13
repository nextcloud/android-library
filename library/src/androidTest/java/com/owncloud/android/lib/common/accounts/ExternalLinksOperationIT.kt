/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.accounts

import com.owncloud.android.AbstractIT
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Test

class ExternalLinksOperationIT : AbstractIT() {
    @Test
    fun retrieveExternalLinks() {
        val result = ExternalLinksOperation().execute(nextcloudClient)
        assertTrue(result.isSuccess)

        val data = result.resultData
        assertEquals(2, data.size)

        assertEquals("Nextcloud", data[0].name)
        assertEquals("https://www.nextcloud.com", data[0].url)

        assertEquals("Forum", data[1].name)
        assertEquals("https://help.nextcloud.com", data[1].url)
    }
}
