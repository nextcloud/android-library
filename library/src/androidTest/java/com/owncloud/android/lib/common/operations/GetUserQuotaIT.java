/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019-2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2015 Bartosz Przybylski <bart.p.pl@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.operations;

import static org.junit.Assert.assertTrue;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.Quota;
import com.owncloud.android.lib.common.UserInfo;
import com.owncloud.android.lib.resources.users.GetUserInfoRemoteOperation;

import org.junit.Test;


/**
 * Class to test Get User Quota
 *
 * @author Bartosz Przybylski
 */
public class GetUserQuotaIT extends AbstractIT {

    @Test
    public void testGetUserQuota() {
        RemoteOperationResult<UserInfo> result = new GetUserInfoRemoteOperation().execute(nextcloudClient);
        assertTrue(result.isSuccess());

        UserInfo userInfo = result.getResultData();
        Quota quota = userInfo.getQuota();
        assertTrue(quota.getFree() >= 0);
        assertTrue(quota.getUsed() >= 0);
        assertTrue(quota.getTotal() > 0);
    }
}
