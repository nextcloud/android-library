/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.recommendations

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation
import org.junit.Assert.assertTrue
import org.junit.Test

class GetRecommendationsRemoteOperationIT : AbstractIT() {
    @Test
    fun getRecommendations() {
        assertTrue(CreateFolderRemoteOperation("/test/", true).execute(client).isSuccess)

        val result = GetRecommendationsRemoteOperation().execute(nextcloudClient).resultData

        assertTrue(result.enabled)
        assertTrue(result.recommendations.isNotEmpty())
    }
}
