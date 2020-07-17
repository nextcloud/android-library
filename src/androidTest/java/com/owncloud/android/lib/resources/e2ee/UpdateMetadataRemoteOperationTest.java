/*
 *
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2020 Tobias Kaminsky
 * Copyright (C) 2020 Nextcloud GmbH
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

package com.owncloud.android.lib.resources.e2ee;

import android.text.TextUtils;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory;
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.ReadFileRemoteOperation;
import com.owncloud.android.lib.resources.files.model.RemoteFile;
import com.owncloud.android.lib.resources.status.GetCapabilitiesRemoteOperation;
import com.owncloud.android.lib.resources.status.OCCapability;
import com.owncloud.android.lib.resources.status.OwnCloudVersion;

import net.bytebuddy.utility.RandomString;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assume.assumeTrue;

public class UpdateMetadataRemoteOperationTest extends AbstractIT {
    @Test
    public void uploadAndModify() {
        // tests only for NC19+
        OCCapability capability = (OCCapability) new GetCapabilitiesRemoteOperation(null)
                .execute(client)
                .getSingleData();

        assumeTrue(capability.getVersion().isNewerOrEqual(OwnCloudVersion.nextcloud_20));

        // E2E server app checks for official NC client with >=3.13.0, 
        // and blocks all other clients, e.g. 3rd party apps using this lib
        OwnCloudClientManagerFactory.setUserAgent("Mozilla/5.0 (Android) Nextcloud-android/3.13.0");

        // create folder
        String folder = "/" + RandomString.make(20) + "/";
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
                .getSingleData()
                .toString();
        assertFalse(TextUtils.isEmpty(token));

        // add metadata
        String expectedMetadata = "metadata";
        assertTrue(new StoreMetadataRemoteOperation(remoteFolder.getLocalId(), expectedMetadata)
                .execute(client)
                .isSuccess());

        // unlock
        assertTrue(new UnlockFileRemoteOperation(remoteFolder.getLocalId(), token).execute(client).isSuccess());

        // verify metadata
        String retrievedMetadata = (String) new GetMetadataRemoteOperation(remoteFolder.getLocalId())
                .execute(client)
                .getSingleData();

        assertEquals(expectedMetadata, retrievedMetadata);

        // Lock 
        token = new LockFileRemoteOperation(remoteFolder.getLocalId())
                .execute(client)
                .getSingleData()
                .toString();
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
}
