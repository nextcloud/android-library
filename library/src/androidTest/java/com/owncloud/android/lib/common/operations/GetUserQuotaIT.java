/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019-2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2015 Bartosz Przybylski <bart.p.pl@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.operations;

import static com.owncloud.android.lib.resources.users.GetUserInfoRemoteOperation.SPACE_UNLIMITED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.Quota;
import com.owncloud.android.lib.common.UserInfo;
import com.owncloud.android.lib.resources.status.GetCapabilitiesRemoteOperation;
import com.owncloud.android.lib.resources.status.NextcloudVersion;
import com.owncloud.android.lib.resources.status.OCCapability;
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

        assertNotNull(quota);

        OCCapability capability = (OCCapability) new GetCapabilitiesRemoteOperation()
            .execute(nextcloudClient).getSingleData();

        if (capability.getVersion().isNewerOrEqual(NextcloudVersion.nextcloud_31)) {
            assertEquals(SPACE_UNLIMITED, quota.getFree());
            assertEquals(SPACE_UNLIMITED, quota.getTotal());
        } else {
            assertTrue(quota.getFree() >= 0);
            assertTrue(quota.getTotal() > 0);
        }
        assertTrue(quota.getUsed() >= 0);
    }
}
