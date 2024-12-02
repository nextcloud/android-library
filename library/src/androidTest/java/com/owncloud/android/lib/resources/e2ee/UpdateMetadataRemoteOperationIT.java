/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.e2ee;

import android.text.TextUtils;

import com.nextcloud.test.RandomStringGenerator;
import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory;
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.ReadFileRemoteOperation;
import com.owncloud.android.lib.resources.files.model.RemoteFile;
import com.owncloud.android.lib.resources.status.OwnCloudVersion;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class UpdateMetadataRemoteOperationIT extends AbstractIT {
    //@Test
    public void uploadAndModifyV1() {
        // tests only for NC19+
        testOnlyOnServer(OwnCloudVersion.nextcloud_20);

        // E2E server app checks for official NC client with >=3.13.0, 
        // and blocks all other clients, e.g. 3rd party apps using this lib
        OwnCloudClientManagerFactory.setUserAgent("Mozilla/5.0 (Android) Nextcloud-android/3.13.0");

        // create folder
        String folder = "/" + RandomStringGenerator.make(20) + "/";
        assertTrue(new CreateFolderRemoteOperation(folder, true).execute(nextcloudClient).isSuccess());
        RemoteFile remoteFolder = (RemoteFile) new ReadFileRemoteOperation(folder).execute(nextcloudClient).getResultData();

        assertNotNull(remoteFolder);

        // mark as encrypted
        assertTrue(new ToggleEncryptionRemoteOperation(remoteFolder.getLocalId(),
                                                       remoteFolder.getRemotePath(),
                                                       true)
                           .execute(nextcloudClient)
                           .isSuccess());

        // Lock 
        String token = new LockFileRemoteOperation(remoteFolder.getLocalId())
                .execute(client)
                .getResultData();
        assertFalse(TextUtils.isEmpty(token));

        // add metadata
        String expectedMetadata = "metadata";
        assertTrue(new StoreMetadataRemoteOperation(remoteFolder.getLocalId(), expectedMetadata)
                           .execute(client)
                           .isSuccess());

        // unlock
        assertTrue(new UnlockFileRemoteOperation(remoteFolder.getLocalId(), token).execute(client).isSuccess());

        // verify metadata
        MetadataResponse retrievedMetadata = new GetMetadataRemoteOperation(remoteFolder.getLocalId())
                .execute(client)
                .getResultData();

        assertEquals(expectedMetadata, retrievedMetadata.getMetadata());

        // Lock 
        token = new LockFileRemoteOperation(remoteFolder.getLocalId())
                .execute(client)
                .getResultData();
        assertFalse(TextUtils.isEmpty(token));

        // update metadata
        String updatedMetadata = "metadata2";
        assertTrue(new UpdateMetadataRemoteOperation(remoteFolder.getLocalId(), updatedMetadata, token)
                           .execute(client)
                           .isSuccess());

        // unlock
        assertTrue(new UnlockFileRemoteOperation(remoteFolder.getLocalId(), token).execute(client).isSuccess());

        // verify metadata
        MetadataResponse retrievedMetadata2 = new GetMetadataRemoteOperation(remoteFolder.getLocalId())
                .execute(client)
                .getResultData();

        assertEquals(updatedMetadata, retrievedMetadata2.getMetadata());
    }

    //@Test
    public void uploadAndModifyV2() {
        // tests only for NC19+
        testOnlyOnServer(OwnCloudVersion.nextcloud_20);

        // E2E server app checks for official NC client with >=3.13.0, 
        // and blocks all other clients, e.g. 3rd party apps using this lib
        OwnCloudClientManagerFactory.setUserAgent("Mozilla/5.0 (Android) Nextcloud-android/3.13.0");

        // create folder
        String folder = "/" + RandomStringGenerator.make(20) + "/";
        assertTrue(new CreateFolderRemoteOperation(folder, true).execute(nextcloudClient).isSuccess());
        RemoteFile remoteFolder = new ReadFileRemoteOperation(folder).execute(nextcloudClient).getResultData();

        assertNotNull(remoteFolder);

        // mark as encrypted
        assertTrue(new ToggleEncryptionRemoteOperation(remoteFolder.getLocalId(),
                                                       remoteFolder.getRemotePath(),
                                                       true)
                           .execute(nextcloudClient)
                           .isSuccess());

        // Lock 
        int counter = 0;
        String token = new LockFileRemoteOperation(remoteFolder.getLocalId())
                .execute(client)
                .getResultData();
        assertFalse(TextUtils.isEmpty(token));

        // add metadata
        String expectedMetadata = "metadata";
        String signature = "signature";

        assertTrue(new StoreMetadataV2RemoteOperation(
                remoteFolder.getRemoteId(),
                expectedMetadata,
                token,
                signature)
                           .execute(client)
                           .isSuccess());

        // unlock
        assertTrue(new UnlockFileRemoteOperation(remoteFolder.getLocalId(), token).execute(client).isSuccess());

        // verify metadata
        MetadataResponse metadataResponse = new GetMetadataRemoteOperation(remoteFolder.getLocalId())
                .execute(client)
                .getResultData();

        assertEquals(signature, metadataResponse.getSignature());
        assertEquals(expectedMetadata, metadataResponse.getMetadata());

        // Lock 
        counter += 1;
        token = new LockFileRemoteOperation(remoteFolder.getLocalId(), counter)
                .execute(client)
                .getResultData();
        assertFalse(TextUtils.isEmpty(token));

        // update metadata
        String updatedMetadata = "metadata2";
        signature = "signature2";
        assertTrue(
                new UpdateMetadataV2RemoteOperation(
                        remoteFolder.getRemoteId(),
                        updatedMetadata,
                        token,
                        signature)
                        .execute(client)
                        .isSuccess());

        // unlock
        assertTrue(new UnlockFileRemoteOperation(remoteFolder.getLocalId(), token).execute(client).isSuccess());

        // verify metadata
        metadataResponse = new GetMetadataRemoteOperation(remoteFolder.getLocalId())
                .execute(client)
                .getResultData();

        assertEquals(signature, metadataResponse.getSignature());
        assertEquals(updatedMetadata, metadataResponse.getMetadata());
    }
}
