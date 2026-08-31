/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files

import com.owncloud.android.AbstractIT
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
        val remotePath = "/folder/"

        assertTrue(CreateFolderRemoteOperation(remotePath, true).execute(nextcloudClient).isSuccess)

        // use ownCloud client for reference
        var result = ReadFileRemoteOperation(remotePath).execute(client)
        assertTrue(result.isSuccess)
        val ocClientFile = result.resultData
        assertEquals(remotePath, ocClientFile.remotePath)

        result = ReadFileRemoteOperation(remotePath).execute(nextcloudClient)
        assertTrue(result.isSuccess)
        val ncClientFile = result.resultData
        assertEquals(remotePath, result.resultData.remotePath)

        assertEquals(ocClientFile.modifiedTimestamp, ncClientFile.modifiedTimestamp)

        assertTrue(remoteFilesEqual(ocClientFile, ncClientFile))
    }

    @Test
    fun testLivePhoto() {
        testOnlyOnServer(NextcloudVersion.nextcloud_28)

        val movieFile = createFile("sample")
        val movieFilePath = "/sampleMovie.mov"
        assertTrue(
            UploadFileRemoteOperation(movieFile, movieFilePath, "video/mov", RANDOM_MTIME)
                .execute(client)
                .isSuccess
        )

        val livePhoto = createFile("sample")
        val livePhotoPath = "/samplePic.jpg"
        assertTrue(
            UploadFileRemoteOperation(livePhoto, livePhotoPath, "image/jpeg", RANDOM_MTIME)
                .execute(client)
                .isSuccess
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

        val movieFileResult = ReadFileRemoteOperation(movieFilePath).execute(nextcloudClient)
        assertTrue(movieFileResult.isSuccess)
        val movieRemoteFile = movieFileResult.resultData

        val livePhotoResult = ReadFileRemoteOperation(livePhotoPath).execute(nextcloudClient)
        assertTrue(livePhotoResult.isSuccess)
        val livePhotoRemoteFile = livePhotoResult.resultData

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
                .execute(client)
                .isSuccess
        )

        // use ownCloud client for reference
        var result = ReadFileRemoteOperation(remotePath).execute(client)
        assertTrue(result.isSuccess)
        val ocClientFile = result.resultData
        assertEquals(remotePath, ocClientFile.remotePath)

        result = ReadFileRemoteOperation(remotePath).execute(nextcloudClient)
        assertTrue(result.isSuccess)
        val ncClientFile = result.resultData
        assertEquals(remotePath, result.resultData.remotePath)

        assertTrue(remoteFilesEqual(ocClientFile, ncClientFile))
    }

    @Test
    fun testMetadata() {
        val filePath = getFile("gps.jpg").absolutePath
        val remotePath = "/gps.jpg"

        assertTrue(
            UploadFileRemoteOperation(filePath, remotePath, "image/jpg", RANDOM_MTIME)
                .execute(client)
                .isSuccess
        )

        val result = ReadFileRemoteOperation(remotePath).execute(nextcloudClient)

        assertTrue(result.isSuccess)
        val remoteFile = result.resultData

        @Suppress("Detekt.MagicNumber")
        if (isServerAtLeast(NextcloudVersion.nextcloud_23)) {
            assertEquals(ImageDimension(451f, 529f), remoteFile.imageDimension)
        }

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
        assertTrue(CreateFolderRemoteOperation(remotePath, true).execute(nextcloudClient).isSuccess)

        var result = ReadFileRemoteOperation(remotePath).execute(nextcloudClient)
        val remoteFile = result.resultData

        assertTrue(result.isSuccess)
        assertFalse(remoteFile.isEncrypted)
        assertEquals(remotePath, remoteFile.remotePath)

        // mark as encrypted
        assertTrue(
            ToggleEncryptionRemoteOperation(
                remoteFile.localId,
                remotePath,
                true
            ).execute(client)
                .isSuccess
        )

        // re-read
        result = ReadFileRemoteOperation(remotePath).execute(nextcloudClient)
        assertEquals(true, result.resultData.isEncrypted)
    }

    private fun remoteFilesEqual(
        a: RemoteFile,
        b: RemoteFile
    ): Boolean =
        a.remotePath == b.remotePath &&
            a.mimeType == b.mimeType &&
            a.length == b.length &&
            a.creationTimestamp == b.creationTimestamp &&
            a.modifiedTimestamp == b.modifiedTimestamp &&
            a.uploadTimestamp == b.uploadTimestamp &&
            a.etag == b.etag &&
            a.permissions == b.permissions &&
            a.localId == b.localId &&
            a.remoteId == b.remoteId &&
            a.size == b.size &&
            a.isFavorite == b.isFavorite &&
            a.isEncrypted == b.isEncrypted &&
            a.mountType == b.mountType &&
            a.ownerId == b.ownerId &&
            a.ownerDisplayName == b.ownerDisplayName &&
            a.unreadCommentsCount == b.unreadCommentsCount &&
            a.isHasPreview == b.isHasPreview &&
            a.note == b.note &&
            a.sharees.contentEquals(b.sharees) &&
            a.richWorkspace == b.richWorkspace &&
            a.isLocked == b.isLocked &&
            a.lockType == b.lockType &&
            a.lockOwner == b.lockOwner &&
            a.lockOwnerDisplayName == b.lockOwnerDisplayName &&
            a.lockTimestamp == b.lockTimestamp &&
            a.lockOwnerEditor == b.lockOwnerEditor &&
            a.lockTimeout == b.lockTimeout &&
            a.lockToken == b.lockToken &&
            a.tags.contentEquals(b.tags) &&
            a.imageDimension == b.imageDimension &&
            a.geoLocation == b.geoLocation &&
            a.hidden == b.hidden &&
            a.livePhoto == b.livePhoto &&
            a.fileDownloadLimit == b.fileDownloadLimit
}
