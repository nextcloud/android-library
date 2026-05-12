/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.e2ee

import android.text.TextUtils
import com.nextcloud.common.defaultSessionTimeOut
import com.nextcloud.test.RandomStringGenerator.make
import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation
import com.owncloud.android.lib.resources.files.ReadFileRemoteOperation
import com.owncloud.android.lib.resources.files.model.RemoteFile
import com.owncloud.android.lib.resources.status.OwnCloudVersion
import junit.framework.TestCase
import org.junit.Assert

@Suppress("LongMethod", "MagicNumber")
class UpdateMetadataRemoteOperationIT : AbstractIT() {
    // @Test
    fun uploadAndModifyV1() {
        // E2E server app checks for official NC client with >=3.13.0,
        // and blocks all other clients, e.g. 3rd party apps using this lib
        OwnCloudClientManagerFactory.setUserAgent("Mozilla/5.0 (Android) Nextcloud-android/3.13.0")

        // create folder
        val folder = "/" + make(20) + "/"
        TestCase.assertTrue(CreateFolderRemoteOperation(folder, true).execute(client).isSuccess)
        val remoteFolder =
            ReadFileRemoteOperation(folder).execute(client).getSingleData() as RemoteFile?

        TestCase.assertNotNull(remoteFolder)

        // mark as encrypted
        TestCase.assertTrue(
            ToggleEncryptionRemoteOperation(
                remoteFolder!!.localId,
                remoteFolder.remotePath,
                true
            ).execute(client)
                .isSuccess
        )

        // Lock
        var token =
            LockFileRemoteOperation(remoteFolder.localId)
                .execute(client)
                .getResultData()
        Assert.assertFalse(TextUtils.isEmpty(token))

        // add metadata
        val expectedMetadata = "metadata"
        TestCase.assertTrue(
            StoreMetadataRemoteOperation(remoteFolder.localId, expectedMetadata)
                .execute(client)
                .isSuccess
        )

        // unlock
        TestCase.assertTrue(
            UnlockFileRemoteOperation(remoteFolder.localId, token).execute(client).isSuccess
        )

        // verify metadata
        val retrievedMetadata =
            GetMetadataRemoteOperation(remoteFolder.localId)
                .execute(client)
                .getResultData()

        TestCase.assertEquals(expectedMetadata, retrievedMetadata.metadata)

        // Lock
        token =
            LockFileRemoteOperation(remoteFolder.localId)
                .execute(client)
                .getResultData()
        Assert.assertFalse(TextUtils.isEmpty(token))

        // update metadata
        val updatedMetadata = "metadata2"
        TestCase.assertTrue(
            UpdateMetadataRemoteOperation(remoteFolder.localId, updatedMetadata, token)
                .execute(client)
                .isSuccess
        )

        // unlock
        TestCase.assertTrue(
            UnlockFileRemoteOperation(remoteFolder.localId, token).execute(client).isSuccess
        )

        // verify metadata
        val retrievedMetadata2 =
            GetMetadataRemoteOperation(remoteFolder.localId)
                .execute(client)
                .getSingleData() as String?

        TestCase.assertEquals(updatedMetadata, retrievedMetadata2)
    }

    // @Test
    fun uploadAndModifyV2() {
        // E2E server app checks for official NC client with >=3.13.0,
        // and blocks all other clients, e.g. 3rd party apps using this lib
        OwnCloudClientManagerFactory.setUserAgent("Mozilla/5.0 (Android) Nextcloud-android/3.13.0")

        // create folder
        val folder = "/" + make(20) + "/"
        TestCase.assertTrue(CreateFolderRemoteOperation(folder, true).execute(client).isSuccess)
        val remoteFolder =
            ReadFileRemoteOperation(folder).execute(client).getSingleData() as RemoteFile?

        TestCase.assertNotNull(remoteFolder)

        // mark as encrypted
        TestCase.assertTrue(
            ToggleEncryptionRemoteOperation(
                remoteFolder!!.localId,
                remoteFolder.remotePath,
                true
            ).execute(client)
                .isSuccess
        )

        // Lock
        var counter = 0
        var token =
            LockFileRemoteOperation(remoteFolder.localId)
                .execute(client)
                .getResultData()
        Assert.assertFalse(TextUtils.isEmpty(token))

        // add metadata
        val expectedMetadata = "metadata"
        var signature = "signature"

        TestCase.assertTrue(
            StoreMetadataV2RemoteOperation(
                remoteFolder.remoteId!!,
                expectedMetadata,
                token,
                signature
            ).execute(client)
                .isSuccess
        )

        // unlock
        TestCase.assertTrue(
            UnlockFileRemoteOperation(remoteFolder.localId, token).execute(client).isSuccess
        )

        // verify metadata
        var metadataResponse =
            GetMetadataRemoteOperation(remoteFolder.localId)
                .execute(client)
                .getResultData()

        TestCase.assertEquals(signature, metadataResponse.signature)
        TestCase.assertEquals(expectedMetadata, metadataResponse.metadata)

        // Lock
        counter += 1
        token =
            LockFileRemoteOperation(remoteFolder.localId, counter.toLong(), defaultSessionTimeOut)
                .execute(client)
                .getResultData()
        Assert.assertFalse(TextUtils.isEmpty(token))

        // update metadata
        val updatedMetadata = "metadata2"
        signature = "signature2"
        TestCase.assertTrue(
            UpdateMetadataV2RemoteOperation(
                remoteFolder.remoteId!!,
                updatedMetadata,
                token,
                signature
            ).execute(client)
                .isSuccess
        )

        // unlock
        TestCase.assertTrue(
            UnlockFileRemoteOperation(remoteFolder.localId, token).execute(client).isSuccess
        )

        // verify metadata
        metadataResponse =
            GetMetadataRemoteOperation(remoteFolder.localId)
                .execute(client)
                .getResultData()

        TestCase.assertEquals(signature, metadataResponse.signature)
        TestCase.assertEquals(updatedMetadata, metadataResponse.metadata)
    }
}
