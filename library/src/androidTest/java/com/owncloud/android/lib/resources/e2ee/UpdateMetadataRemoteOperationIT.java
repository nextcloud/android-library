/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2023 Tobias Kaminsky
 *   Copyright (C) 2023 Nextcloud GmbH
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

package com.owncloud.android.lib.resources.e2ee;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import android.text.TextUtils;

import com.nextcloud.test.RandomStringGenerator;
import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory;
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.ReadFileRemoteOperation;
import com.owncloud.android.lib.resources.files.model.RemoteFile;
import com.owncloud.android.lib.resources.status.OwnCloudVersion;

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
        assertTrue(new CreateFolderRemoteOperation(folder, true).execute(client).isSuccess());
        RemoteFile remoteFolder = (RemoteFile) new ReadFileRemoteOperation(folder).execute(client).getSingleData();

        assertNotNull(remoteFolder);

        // mark as encrypted
        assertTrue(new ToggleEncryptionRemoteOperation(remoteFolder.getLocalId(),
                remoteFolder.getRemotePath(),
                true)
                .execute(client)
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
        String retrievedMetadata2 = (String) new GetMetadataRemoteOperation(remoteFolder.getLocalId())
                .execute(client)
                .getSingleData();

        assertEquals(updatedMetadata, retrievedMetadata2);
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
        assertTrue(new CreateFolderRemoteOperation(folder, true).execute(client).isSuccess());
        RemoteFile remoteFolder = (RemoteFile) new ReadFileRemoteOperation(folder).execute(client).getSingleData();

        assertNotNull(remoteFolder);

        // mark as encrypted
        assertTrue(new ToggleEncryptionRemoteOperation(remoteFolder.getLocalId(),
                remoteFolder.getRemotePath(),
                true)
                .execute(client)
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
