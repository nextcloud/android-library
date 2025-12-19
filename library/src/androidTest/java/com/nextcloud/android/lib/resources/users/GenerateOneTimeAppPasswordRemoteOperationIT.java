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

import android.text.TextUtils;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;

import org.junit.Test;

public class GenerateOneTimeAppPasswordRemoteOperationIT extends AbstractIT {

    @Test
    public void generateAppPassword() {
        // nc://onetime-login/user:user1&password:Z8i8J-QLDbr-mSn9A-ijXzN-NSBSt&server:https://qr.ltd3.nextcloud.com

        // nextcloudClient.setBaseUri(Uri.parse("https://qr.ltd3.nextcloud.com"));
        // nextcloudClient.setCredentials(Credentials.basic("user1", "user1"));

        GenerateOneTimeAppPasswordRemoteOperation sut = new GenerateOneTimeAppPasswordRemoteOperation();
        RemoteOperationResult<String> result = sut.execute(nextcloudClient);

        assertTrue(result.isSuccess());

        String appPassword = result.getResultData();
        assertFalse(TextUtils.isEmpty(appPassword));

        // re-using onetime password should fail
        assertFalse(new GenerateOneTimeAppPasswordRemoteOperation().execute(nextcloudClient).isSuccess());
    }
}
