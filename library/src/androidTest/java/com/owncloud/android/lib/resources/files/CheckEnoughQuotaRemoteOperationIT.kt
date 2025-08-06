/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2023 Tobias Kaminsky
 * Copyright (C) 2023 Nextcloud GmbH
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

package com.owncloud.android.lib.resources.files

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.common.OwnCloudBasicCredentials
import com.owncloud.android.lib.common.OwnCloudClientFactory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckEnoughQuotaRemoteOperationIT : AbstractIT() {
    @Test
    fun enoughQuota() {
        val sut = CheckEnoughQuotaRemoteOperation("/", LARGE_FILE).execute(client)
        assertTrue(sut.isSuccess)
    }

    @Test
    fun noQuota() {
        // user3 has only 1M quota
        val client3 = OwnCloudClientFactory.createOwnCloudClient(url, context, true)
        client3.credentials = OwnCloudBasicCredentials("user3", "user3")
        val sut = CheckEnoughQuotaRemoteOperation("/", LARGE_FILE).execute(client3)
        assertFalse(sut.isSuccess)
    }

    companion object {
        const val LARGE_FILE = 5 * 1024 * 1024L
    }
}
