/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.users;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotEquals;

import android.text.TextUtils;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.OwnCloudBasicCredentials;
import com.owncloud.android.lib.common.OwnCloudCredentials;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation;

import org.junit.Test;

public class GenerateAppPasswordRemoteOperationIT extends AbstractIT {

    @Test
    public void generateAppPassword() {
        GenerateAppPasswordRemoteOperation sut = new GenerateAppPasswordRemoteOperation();
        RemoteOperationResult result = sut.execute(client);

        assertTrue(result.isSuccess());

        String appPassword = (String) result.getSingleData();
        assertFalse(TextUtils.isEmpty(appPassword));

        OwnCloudCredentials oldOwnCloudCredentials = client.getCredentials();
        OwnCloudCredentials newOwnCloudCredentials = new OwnCloudBasicCredentials(oldOwnCloudCredentials.getUsername(),
                appPassword);

        assertNotEquals(oldOwnCloudCredentials, newOwnCloudCredentials);

        client.setCredentials(newOwnCloudCredentials);

        assertTrue(new ReadFolderRemoteOperation("/").execute(client).isSuccess());

        // using app password to generate new password should fail
        assertFalse(new GenerateAppPasswordRemoteOperation().execute(client).isSuccess());
    }
}
