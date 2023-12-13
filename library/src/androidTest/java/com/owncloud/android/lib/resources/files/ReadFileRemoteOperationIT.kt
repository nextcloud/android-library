/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
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
    fun testLivePhoto() {
        testOnlyOnServer(NextcloudVersion.nextcloud_28)

        val movieFile = createFile("sample")
        val movieFilePath = "/sampleMovie.mov"
        assertTrue(
            UploadFileRemoteOperation(movieFile, movieFilePath, "video/mov", RANDOM_MTIME)
                .execute(client).isSuccess
        )

        val livePhoto = createFile("sample")
        val livePhotoPath = "/samplePic.jpg"
        assertTrue(
            UploadFileRemoteOperation(livePhoto, livePhotoPath, "image/jpeg", RANDOM_MTIME)
                .execute(client).isSuccess
        )

        // link them
        assertTrue(
            LinkLivePhotoRemoteOperation(
                livePhotoPath,
                movieFilePath
            ).execute(client).isSuccess
        )

        assertTrue(
            LinkLivePhotoRemoteOperation(
                movieFilePath,
                livePhotoPath
            ).execute(client).isSuccess
        )

        val movieFileResult = ReadFileRemoteOperation(movieFilePath).execute(client)
        assertTrue(movieFileResult.isSuccess)
        val movieRemoteFile = movieFileResult.data[0] as RemoteFile

        val livePhotoResult = ReadFileRemoteOperation(livePhotoPath).execute(client)
        assertTrue(livePhotoResult.isSuccess)
        val livePhotoRemoteFile = livePhotoResult.data[0] as RemoteFile

        assertEquals(livePhotoRemoteFile.livePhoto, movieRemoteFile.remotePath)
        assertTrue(movieRemoteFile.hidden)
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

        val ocCapability = GetCapabilitiesRemoteOperation().execute(nextcloudClient).resultData

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
