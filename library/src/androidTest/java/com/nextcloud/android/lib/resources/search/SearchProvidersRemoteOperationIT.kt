/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2022 Tobias Kaminsky
 *   Copyright (C) 2022 Nextcloud GmbH
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
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
                .execute(nextcloudClient).singleData as OCCapability
        assumeTrue(
            ocCapability.version.isOlderThan(OwnCloudVersion.nextcloud_20)
        )

        val result = nextcloudClient.execute(UnifiedSearchProvidersRemoteOperation())
        assertFalse(result.isSuccess)
    }
}
