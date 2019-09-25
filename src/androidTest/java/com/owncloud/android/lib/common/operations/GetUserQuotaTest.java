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
package com.owncloud.android.lib.common.operations;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.Quota;
import com.owncloud.android.lib.common.UserInfo;
import com.owncloud.android.lib.resources.users.GetUserInfoRemoteOperation;

import org.junit.Test;

import static org.junit.Assert.assertTrue;


/**
 * Class to test Get User Quota
 *
 * @author Bartosz Przybylski
 */
public class GetUserQuotaTest extends AbstractIT {

    @Test
    public void testGetUserQuota() {
        RemoteOperationResult result = new GetUserInfoRemoteOperation().execute(client);
        assertTrue(result.isSuccess());
        assertTrue(result.getData() != null && result.getData().size() == 1);

        UserInfo userInfo = (UserInfo) result.getData().get(0);
        Quota quota = userInfo.getQuota();
        assertTrue(quota.getFree() >= 0);
        assertTrue(quota.getUsed() >= 0);
        assertTrue(quota.getTotal() > 0);
    }
}
