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

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.UserInfo;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SetUserInfoRemoteOperationTest extends AbstractIT {
    @Test
    public void testSetEmail() {
        RemoteOperationResult userInfo = new GetUserInfoRemoteOperation().execute(client);
        assertTrue(userInfo.isSuccess());
        String oldValue = ((UserInfo) userInfo.getData().get(0)).getEmail();

        // set
        assertTrue(new SetUserInfoRemoteOperation(SetUserInfoRemoteOperation.Field.EMAIL, "new@mail.com")
                           .execute(client).isSuccess());

        userInfo = new GetUserInfoRemoteOperation().execute(client);
        assertTrue(userInfo.isSuccess());
        assertEquals("new@mail.com", ((UserInfo) userInfo.getData().get(0)).email);

        // reset
        assertTrue(new SetUserInfoRemoteOperation(SetUserInfoRemoteOperation.Field.EMAIL, oldValue)
                           .execute(client).isSuccess());
    }

    @Test
    public void testSetDisplayName() {
        RemoteOperationResult userInfo = new GetUserInfoRemoteOperation().execute(client);
        assertTrue(userInfo.isSuccess());

        String oldUserId = client.getUserId();
        assertEquals(client.getUserId(), ((UserInfo) userInfo.getData().get(0)).displayName);

        // set display name
        assertTrue(new SetUserInfoRemoteOperation(SetUserInfoRemoteOperation.Field.DISPLAYNAME, "newName")
                           .execute(client).isSuccess());

        userInfo = new GetUserInfoRemoteOperation().execute(client);
        assertTrue(userInfo.isSuccess());
        assertEquals("newName", ((UserInfo) userInfo.getData().get(0)).displayName);

        // reset
        assertTrue(new SetUserInfoRemoteOperation(SetUserInfoRemoteOperation.Field.DISPLAYNAME, oldUserId)
                           .execute(client).isSuccess());
    }

    @Test
    public void testSetPhone() {
        RemoteOperationResult userInfo = new GetUserInfoRemoteOperation().execute(client);
        assertTrue(userInfo.isSuccess());
        String oldValue = ((UserInfo) userInfo.getData().get(0)).phone;

        // set
        assertTrue(new SetUserInfoRemoteOperation(SetUserInfoRemoteOperation.Field.PHONE, "555-12345")
                           .execute(client).isSuccess());

        userInfo = new GetUserInfoRemoteOperation().execute(client);
        assertTrue(userInfo.isSuccess());
        assertEquals("555-12345", ((UserInfo) userInfo.getData().get(0)).phone);

        // reset
        assertTrue(new SetUserInfoRemoteOperation(SetUserInfoRemoteOperation.Field.PHONE, oldValue)
                           .execute(client).isSuccess());
    }

    @Test
    public void testSetAddress() {
        RemoteOperationResult userInfo = new GetUserInfoRemoteOperation().execute(client);
        assertTrue(userInfo.isSuccess());
        String oldValue = ((UserInfo) userInfo.getData().get(0)).address;

        // set
        assertTrue(new SetUserInfoRemoteOperation(SetUserInfoRemoteOperation.Field.ADDRESS, "NoName Street 123")
                           .execute(client).isSuccess());

        userInfo = new GetUserInfoRemoteOperation().execute(client);
        assertTrue(userInfo.isSuccess());
        assertEquals("NoName Street 123", ((UserInfo) userInfo.getData().get(0)).address);

        // reset
        assertTrue(new SetUserInfoRemoteOperation(SetUserInfoRemoteOperation.Field.ADDRESS, oldValue)
                           .execute(client).isSuccess());
    }

    @Test
    public void testSetWebsite() {
        RemoteOperationResult userInfo = new GetUserInfoRemoteOperation().execute(client);
        assertTrue(userInfo.isSuccess());
        String oldValue = ((UserInfo) userInfo.getData().get(0)).website;

        // set
        assertTrue(new SetUserInfoRemoteOperation(SetUserInfoRemoteOperation.Field.WEBSITE, "https://nextcloud.com")
                           .execute(client).isSuccess());

        userInfo = new GetUserInfoRemoteOperation().execute(client);
        assertTrue(userInfo.isSuccess());
        assertEquals("https://nextcloud.com", ((UserInfo) userInfo.getData().get(0)).website);

        // reset
        assertTrue(new SetUserInfoRemoteOperation(SetUserInfoRemoteOperation.Field.WEBSITE, oldValue)
                           .execute(client).isSuccess());
    }

    @Test
    public void testSetTwitter() {
        RemoteOperationResult userInfo = new GetUserInfoRemoteOperation().execute(client);
        assertTrue(userInfo.isSuccess());
        String oldValue = ((UserInfo) userInfo.getData().get(0)).twitter;

        // set
        assertTrue(new SetUserInfoRemoteOperation(SetUserInfoRemoteOperation.Field.TWITTER, "@Nextclouders")
                           .execute(client).isSuccess());

        userInfo = new GetUserInfoRemoteOperation().execute(client);
        assertTrue(userInfo.isSuccess());
        assertEquals("@Nextclouders", ((UserInfo) userInfo.getData().get(0)).twitter);

        // reset
        assertTrue(new SetUserInfoRemoteOperation(SetUserInfoRemoteOperation.Field.TWITTER, oldValue)
                           .execute(client).isSuccess());
    }
}
