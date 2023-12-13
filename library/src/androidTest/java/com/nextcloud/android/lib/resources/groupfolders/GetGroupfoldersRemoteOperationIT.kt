/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.groupfolders

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.status.GetCapabilitiesRemoteOperation
import com.owncloud.android.lib.resources.status.OCCapability
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test

class GetGroupfoldersRemoteOperationIT : AbstractIT() {
    @Test
    fun getGroupfolders() {
        val capability = GetCapabilitiesRemoteOperation().execute(nextcloudClient).resultData as OCCapability

        assumeTrue(capability.groupfolders.isTrue)

        val map = GetGroupfoldersRemoteOperation().execute(nextcloudClient).resultData
        assertEquals(1, map.size)
        assertEquals("groupfolder", map["1"]?.mountPoint)
    }
}
