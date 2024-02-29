/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.tags

import com.nextcloud.test.RandomStringGenerator
import com.owncloud.android.AbstractIT
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class GetTagsRemoteOperationIT : AbstractIT() {
    companion object {
        const val TAG_LENGTH = 10
    }

    @Test
    fun list() {
        var sut = GetTagsRemoteOperation().execute(client)
        assertTrue(sut.isSuccess)

        val count = sut.resultData.size

        assertTrue(
            CreateTagRemoteOperation(RandomStringGenerator.make(TAG_LENGTH))
                .execute(nextcloudClient)
                .isSuccess
        )

        sut = GetTagsRemoteOperation().execute(client)
        assertTrue(sut.isSuccess)
        assertEquals(count + 1, sut.resultData.size)
    }
}
