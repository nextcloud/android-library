/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.search

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.status.GetCapabilitiesRemoteOperation
import com.owncloud.android.lib.resources.status.OCCapability
import com.owncloud.android.lib.resources.status.OwnCloudVersion
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

class SearchProvidersRemoteOperationIT : AbstractIT() {
    @Test
    fun getSearchProviders() {
        // only on NC20+
        testOnlyOnServer(OwnCloudVersion.nextcloud_20)

        val result = nextcloudClient.execute(UnifiedSearchProvidersRemoteOperation())
        assertTrue(result.isSuccess)

        val providers = result.resultData

        assertTrue(providers.eTag.isNotBlank())
        assertTrue(providers.providers.isNotEmpty())
        assertNotNull(providers.providers.find { it.name == "Files" })
        assertNull(providers.providers.find { it.name == "RandomSearchProvider" })
    }

    @Test
    fun getSearchProvidersOnOldServer() {
        // only on < NC20
        val ocCapability =
            GetCapabilitiesRemoteOperation()
                .execute(nextcloudClient)
                .singleData as OCCapability
        assumeTrue(
            ocCapability.version.isOlderThan(OwnCloudVersion.nextcloud_20)
        )

        val result = nextcloudClient.execute(UnifiedSearchProvidersRemoteOperation())
        assertFalse(result.isSuccess)
    }
}
