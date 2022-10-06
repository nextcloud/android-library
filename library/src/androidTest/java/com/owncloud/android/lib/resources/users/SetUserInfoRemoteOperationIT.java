/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Unpublished <unpublished@users.noreply.github.com>
 * SPDX-FileCopyrightText: 2020-2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2018 Bartosz Przybylski <bart.p.pl@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.users;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.UserInfo;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.status.GetCapabilitiesRemoteOperation;
import com.owncloud.android.lib.resources.status.NextcloudVersion;
import com.owncloud.android.lib.resources.status.OCCapability;

import org.junit.Test;

public class SetUserInfoRemoteOperationIT extends AbstractIT {
    @Test
    public void testSetEmail() {
        RemoteOperationResult<UserInfo> userInfo = new GetUserInfoRemoteOperation().execute(nextcloudClient);
        assertTrue(userInfo.isSuccess());
        String oldValue = userInfo.getResultData().getEmail();

        // set
        assertTrue(new SetUserInfoRemoteOperation(SetUserInfoRemoteOperation.Field.EMAIL, "new@mail.com")
                .execute(nextcloudClient).isSuccess());

        userInfo = new GetUserInfoRemoteOperation().execute(nextcloudClient);
        assertTrue(userInfo.isSuccess());
        assertEquals("new@mail.com", userInfo.getResultData().getEmail());

        // reset
        assertTrue(new SetUserInfoRemoteOperation(SetUserInfoRemoteOperation.Field.EMAIL, oldValue)
                .execute(nextcloudClient).isSuccess());
    }

    @Test
    public void testSetDisplayName() {
        RemoteOperationResult<UserInfo> userInfo = new GetUserInfoRemoteOperation().execute(nextcloudClient);
        assertTrue(userInfo.isSuccess());

        String oldUserId = nextcloudClient.getUserId();
        assertEquals(nextcloudClient.getUserId(), userInfo.getResultData().getDisplayName());

        // set display name
        assertTrue(new SetUserInfoRemoteOperation(SetUserInfoRemoteOperation.Field.DISPLAYNAME, "newName")
                .execute(nextcloudClient).isSuccess());

        userInfo = new GetUserInfoRemoteOperation().execute(nextcloudClient);
        assertTrue(userInfo.isSuccess());
        assertEquals("newName", userInfo.getResultData().getDisplayName());

        // reset
        assertTrue(new SetUserInfoRemoteOperation(SetUserInfoRemoteOperation.Field.DISPLAYNAME, oldUserId)
                .execute(nextcloudClient).isSuccess());
    }

    @Test
    public void testSetPhone() {
        RemoteOperationResult<OCCapability> result = new GetCapabilitiesRemoteOperation().execute(nextcloudClient);
        assertTrue(result.isSuccess());
        OCCapability ocCapability = result.getResultData();
        assertNotNull(ocCapability);

        RemoteOperationResult<UserInfo> userInfo = new GetUserInfoRemoteOperation().execute(nextcloudClient);
        assertTrue(userInfo.isSuccess());
        String oldValue = userInfo.getResultData().getPhone();

        // set
        assertTrue(new SetUserInfoRemoteOperation(SetUserInfoRemoteOperation.Field.PHONE, "+49555-12345")
                .execute(nextcloudClient).isSuccess());

        userInfo = new GetUserInfoRemoteOperation().execute(nextcloudClient);
        assertTrue(userInfo.isSuccess());

        if (ocCapability.getVersion().isNewerOrEqual(NextcloudVersion.nextcloud_21)) {
            assertEquals("+4955512345", userInfo.getResultData().getPhone());
        } else {
            assertEquals("+49555-12345", userInfo.getResultData().getPhone());
        }

        // reset
        assertTrue(new SetUserInfoRemoteOperation(SetUserInfoRemoteOperation.Field.PHONE, oldValue)
                .execute(nextcloudClient).isSuccess());
    }

    @Test
    public void testSetAddress() {
        RemoteOperationResult<UserInfo> userInfo = new GetUserInfoRemoteOperation().execute(nextcloudClient);
        assertTrue(userInfo.isSuccess());
        String oldValue = userInfo.getResultData().getAddress();

        // set
        assertTrue(new SetUserInfoRemoteOperation(SetUserInfoRemoteOperation.Field.ADDRESS, "NoName Street 123")
                .execute(nextcloudClient).isSuccess());

        userInfo = new GetUserInfoRemoteOperation().execute(nextcloudClient);
        assertTrue(userInfo.isSuccess());
        assertEquals("NoName Street 123", userInfo.getResultData().getAddress());

        // reset
        assertTrue(new SetUserInfoRemoteOperation(SetUserInfoRemoteOperation.Field.ADDRESS, oldValue)
                .execute(nextcloudClient).isSuccess());
    }

    @Test
    public void testSetWebsite() {
        RemoteOperationResult<UserInfo> userInfo = new GetUserInfoRemoteOperation().execute(nextcloudClient);
        assertTrue(userInfo.isSuccess());
        String oldValue = userInfo.getResultData().getWebsite();

        // set
        assertTrue(new SetUserInfoRemoteOperation(SetUserInfoRemoteOperation.Field.WEBSITE, "https://nextcloud.com")
                .execute(nextcloudClient).isSuccess());

        userInfo = new GetUserInfoRemoteOperation().execute(nextcloudClient);
        assertTrue(userInfo.isSuccess());
        assertEquals("https://nextcloud.com", userInfo.getResultData().getWebsite());

        // reset
        assertTrue(new SetUserInfoRemoteOperation(SetUserInfoRemoteOperation.Field.WEBSITE, oldValue)
                .execute(nextcloudClient).isSuccess());
    }

    @Test
    public void testSetTwitter() {
        RemoteOperationResult<UserInfo> userInfo = new GetUserInfoRemoteOperation().execute(nextcloudClient);
        assertTrue(userInfo.isSuccess());
        String oldValue = userInfo.getResultData().getTwitter();

        // set
        assertTrue(new SetUserInfoRemoteOperation(SetUserInfoRemoteOperation.Field.TWITTER, "@Nextclouders")
                .execute(nextcloudClient).isSuccess());

        userInfo = new GetUserInfoRemoteOperation().execute(nextcloudClient);
        assertTrue(userInfo.isSuccess());
        assertEquals("@Nextclouders", userInfo.getResultData().getTwitter());

        // reset
        assertTrue(new SetUserInfoRemoteOperation(SetUserInfoRemoteOperation.Field.TWITTER, oldValue)
                .execute(nextcloudClient).isSuccess());
    }
}
