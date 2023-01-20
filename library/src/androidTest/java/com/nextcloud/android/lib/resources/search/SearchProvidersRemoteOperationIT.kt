/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2020 Tobias Kaminsky
 * Copyright (C) 2020 Nextcloud GmbH
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
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
        val ocCapability = GetCapabilitiesRemoteOperation()
            .execute(nextcloudClient).singleData as OCCapability
        assumeTrue(ocCapability.version.isNewerOrEqual(OwnCloudVersion.nextcloud_20))

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
        val ocCapability = GetCapabilitiesRemoteOperation()
            .execute(nextcloudClient).singleData as OCCapability
        assumeTrue(
            ocCapability.version.isOlderThan(OwnCloudVersion.nextcloud_20)
        )

        val result = nextcloudClient.execute(UnifiedSearchProvidersRemoteOperation())
        assertFalse(result.isSuccess)
    }
}
