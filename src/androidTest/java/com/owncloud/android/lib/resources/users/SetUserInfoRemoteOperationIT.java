/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2019 Tobias Kaminsky
 *   Copyright (C) 2019 Nextcloud GmbH
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

package com.owncloud.android.lib.resources.users;

import static org.junit.Assert.assertEquals;
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
        RemoteOperationResult result = new GetCapabilitiesRemoteOperation().execute(nextcloudClient);
        assertTrue(result.isSuccess());
        OCCapability ocCapability = (OCCapability) result.getSingleData();

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
