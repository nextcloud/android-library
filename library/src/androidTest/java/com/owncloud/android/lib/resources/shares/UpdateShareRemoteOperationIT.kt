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
package com.owncloud.android.lib.resources.shares

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation
import com.owncloud.android.lib.resources.files.RemoveFileRemoteOperation
import com.owncloud.android.lib.resources.status.GetCapabilitiesRemoteOperation
import com.owncloud.android.lib.resources.status.NextcloudVersion
import com.owncloud.android.lib.resources.status.OCCapability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateShareRemoteOperationIT : AbstractIT() {
    @Test
    fun shortNote() {
        testUpdateNote("123")
    }

    @Test
    fun middleNote() {
        testUpdateNote("123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123")
    }

    @Test
    fun longNote() {
        testUpdateNote(
            "123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123" +
                "123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123" +
                "123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123" +
                "123123123123123123123123123123123123123123123123123123123123123123123123123123123123123123"
        )
    }

    @Test
    fun testEscapedNote() {
        testUpdateNote("test & test")
    }

    private fun testUpdateNote(note: String) {
        assertTrue(CreateFolderRemoteOperation("/note/", true).execute(client).isSuccess)

        // share folder to user "admin"
        val createOperationResult = CreateShareRemoteOperation(
            "/note/",
            ShareType.USER,
            "admin",
            false,
            "",
            OCShare.MAXIMUM_PERMISSIONS_FOR_FOLDER,
            true
        ).execute(client)

        assertTrue(createOperationResult.isSuccess)

        val share = createOperationResult.resultData[0]

        val sut = UpdateShareRemoteOperation(share.remoteId)
        sut.setNote(note)

        assertTrue(sut.execute(client).isSuccess)

        // verify
        val getShareOperationResult = GetShareRemoteOperation(share.remoteId).execute(client)
        assertTrue(getShareOperationResult.isSuccess)

        val updatedShare = getShareOperationResult.resultData[0]

        assertEquals(note, updatedShare.note)

        assertTrue(RemoveFileRemoteOperation("/note/").execute(client).isSuccess)
    }

    @Test
    fun updateLabel() {
        val label = "test & test"
        assertTrue(CreateFolderRemoteOperation("/label/", true).execute(client).isSuccess)

        // share folder via public link
        val createOperationResult = CreateShareRemoteOperation(
            "/label/",
            ShareType.PUBLIC_LINK,
            "",
            true,
            "",
            OCShare.READ_PERMISSION_FLAG
        ).execute(client)

        assertTrue(createOperationResult.isSuccess)

        val share = createOperationResult.resultData[0]

        val sut = UpdateShareRemoteOperation(share.remoteId)
        sut.setLabel(label)

        assertTrue(sut.execute(client).isSuccess)

        // verify
        val getShareOperationResult = GetShareRemoteOperation(share.remoteId).execute(client)
        assertTrue(getShareOperationResult.isSuccess)

        val updatedShare = getShareOperationResult.resultData[0]

        assertEquals(label, updatedShare.label)

        assertTrue(RemoveFileRemoteOperation("/label/").execute(client).isSuccess)
    }

    @Test
    @Suppress("MaxLineLength")
    fun invalidPassword() {
        val folder = "/invalidPassword/"
        assertTrue(CreateFolderRemoteOperation(folder, true).execute(client).isSuccess)

        // share folder via public link
        val createOperationResult = CreateShareRemoteOperation(
            folder,
            ShareType.PUBLIC_LINK,
            "",
            true,
            "",
            OCShare.READ_PERMISSION_FLAG
        ).execute(client)

        assertTrue(createOperationResult.isSuccess)

        val share = createOperationResult.resultData[0]

        val sut = UpdateShareRemoteOperation(share.remoteId)
        sut.setPassword("1")

        val result = sut.execute(client)
        assertFalse(result.isSuccess)

        val capabilityResult = GetCapabilitiesRemoteOperation().execute(nextcloudClient)
        assertTrue(capabilityResult.isSuccess)
        val capability = capabilityResult.singleData as OCCapability

        when {
            capability.version.isNewerOrEqual(NextcloudVersion.nextcloud_22) -> {
                assertEquals(
                    "Password needs to be at least 10 characters long. Password is present in compromised password list. Please choose a different password.",
                    result.message
                )
            }
            capability.version.isNewerOrEqual(NextcloudVersion.nextcloud_21) -> {
                assertEquals("Password needs to be at least 8 characters long.", result.message)
            }
            else -> {
                assertEquals("Password needs to be at least 8 characters long", result.message)
            }
        }

        assertTrue(RemoveFileRemoteOperation(folder).execute(client).isSuccess)
    }

    @Test
    fun validPassword() {
        val folder = "/validPassword/"
        assertTrue(CreateFolderRemoteOperation(folder, true).execute(client).isSuccess)

        // share folder via public link
        val createOperationResult = CreateShareRemoteOperation(
            folder,
            ShareType.PUBLIC_LINK,
            "",
            true,
            "",
            OCShare.READ_PERMISSION_FLAG
        ).execute(client)

        assertTrue(createOperationResult.isSuccess)

        val share = createOperationResult.resultData[0]

        val sut = UpdateShareRemoteOperation(share.remoteId)
        sut.setPassword("arnservcvcbtp234")

        assertTrue(sut.execute(client).isSuccess)
        assertTrue(RemoveFileRemoteOperation(folder).execute(client).isSuccess)
    }
}
