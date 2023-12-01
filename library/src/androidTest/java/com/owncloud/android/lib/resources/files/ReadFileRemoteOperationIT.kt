/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2021 Tobias Kaminsky
 * Copyright (C) 2021 Nextcloud GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package com.owncloud.android.lib.resources.files

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory
import com.owncloud.android.lib.resources.e2ee.ToggleEncryptionRemoteOperation
import com.owncloud.android.lib.resources.files.model.GeoLocation
import com.owncloud.android.lib.resources.files.model.ImageDimension
import com.owncloud.android.lib.resources.files.model.RemoteFile
import com.owncloud.android.lib.resources.status.GetCapabilitiesRemoteOperation
import com.owncloud.android.lib.resources.status.NextcloudVersion
import com.owncloud.android.lib.resources.status.OCCapability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadFileRemoteOperationIT : AbstractIT() {
    @Test
    fun readRemoteFolder() {
        val remotePath = "/test/"

        assertTrue(CreateFolderRemoteOperation(remotePath, true).execute(client).isSuccess)

        val result = ReadFileRemoteOperation(remotePath).execute(client)

        assertTrue(result.isSuccess)
        assertEquals(remotePath, (result.data[0] as RemoteFile).remotePath)
    }

    @Test
    fun readRemoteFile() {
        // create file
        val filePath = createFile("text")
        val remotePath = "/test.md"
        assertTrue(
            UploadFileRemoteOperation(filePath, remotePath, "text/markdown", RANDOM_MTIME)
                .execute(client).isSuccess
        )

        val result = ReadFileRemoteOperation(remotePath).execute(client)

        assertTrue(result.isSuccess)
        assertEquals(remotePath, (result.data[0] as RemoteFile).remotePath)
    }

    @Test
    fun testMetadata() {
        val filePath = getFile("gps.jpg").absolutePath
        val remotePath = "/gps.jpg"

        assertTrue(
            UploadFileRemoteOperation(filePath, remotePath, "image/jpg", RANDOM_MTIME)
                .execute(client).isSuccess
        )

        val result = ReadFileRemoteOperation(remotePath).execute(client)

        assertTrue(result.isSuccess)
        val remoteFile = result.data[0] as RemoteFile

        @Suppress("Detekt.MagicNumber")
        assertEquals(ImageDimension(451f, 529f), remoteFile.imageDimension)

        testOnlyOnServer(NextcloudVersion.nextcloud_27)

        val ocCapability =
            GetCapabilitiesRemoteOperation()
                .execute(nextcloudClient)
                .singleData as OCCapability

        if (ocCapability.version.majorVersionNumber == NextcloudVersion.nextcloud_27.majorVersionNumber) {
            @Suppress("Detekt.MagicNumber")
            assertEquals(GeoLocation(49.99679166666667, 8.67198611111111), remoteFile.geoLocation)
        } else {
            @Suppress("Detekt.MagicNumber")
            assertEquals(GeoLocation(49.996791666667, 8.6719861111111), remoteFile.geoLocation)
        }
    }

    @Test
    fun readEncryptedState() {
        val remotePath = "/testEncryptedFolder/"

        // E2E server app checks for official NC client with >=3.13.0,
        // and blocks all other clients, e.g. 3rd party apps using this lib
        OwnCloudClientManagerFactory.setUserAgent("Mozilla/5.0 (Android) Nextcloud-android/3.13.0")

        assertTrue(CreateFolderRemoteOperation(remotePath, true).execute(client).isSuccess)

        var result = ReadFileRemoteOperation(remotePath).execute(client)
        val remoteFile = result.data[0] as RemoteFile

        assertTrue(result.isSuccess)
        assertFalse(remoteFile.isEncrypted)
        assertEquals(remotePath, remoteFile.remotePath)

        // mark as encrypted
        assertTrue(
            ToggleEncryptionRemoteOperation(
                remoteFile.localId,
                remotePath,
                true
            )
                .execute(client)
                .isSuccess
        )

        // re-read
        result = ReadFileRemoteOperation(remotePath).execute(client)
        assertEquals(true, (result.data[0] as RemoteFile).isEncrypted)
    }
}
