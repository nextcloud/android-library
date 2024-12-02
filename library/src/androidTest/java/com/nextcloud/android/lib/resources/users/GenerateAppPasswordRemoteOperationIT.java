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
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation;

import org.junit.Test;

import okhttp3.Credentials;

public class GenerateAppPasswordRemoteOperationIT extends AbstractIT {

    @Test
    public void generateAppPassword() {
        GenerateAppPasswordRemoteOperation sut = new GenerateAppPasswordRemoteOperation();
        RemoteOperationResult<String> result = sut.execute(nextcloudClient);

        assertTrue(result.isSuccess());

        String appPassword = result.getResultData();
        assertFalse(TextUtils.isEmpty(appPassword));

        String clientCredentials = nextcloudClient.getCredentials();
        String newClientCredentials = Credentials.basic(nextcloudClient.getUserId(), appPassword);

        assertNotEquals(clientCredentials, newClientCredentials);

        nextcloudClient.setCredentials(newClientCredentials);

        assertTrue(new ReadFolderRemoteOperation("/").execute(nextcloudClient).isSuccess());

        // using app password to generate new password should fail
        assertFalse(new GenerateAppPasswordRemoteOperation().execute(nextcloudClient).isSuccess());
    }
}
