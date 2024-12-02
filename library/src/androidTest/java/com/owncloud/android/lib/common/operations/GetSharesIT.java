/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019-2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2014-2015 masensio <masensio@solidgear.es>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation;
import com.owncloud.android.lib.resources.shares.CreateShareRemoteOperation;
import com.owncloud.android.lib.resources.shares.GetSharesForFileRemoteOperation;
import com.owncloud.android.lib.resources.shares.GetSharesRemoteOperation;
import com.owncloud.android.lib.resources.shares.OCShare;
import com.owncloud.android.lib.resources.shares.ShareType;

import org.junit.Test;

import java.util.List;

/**
 * Class to test Get Shares Operation
 *
 * @author masensio
 */
public class GetSharesIT extends AbstractIT {
    @Test
    public void testGetShares() {
        assertTrue(new CreateFolderRemoteOperation("/0/", true).execute(nextcloudClient).isSuccess());
        assertTrue(new CreateFolderRemoteOperation("/1/", true).execute(nextcloudClient).isSuccess());
        assertTrue(new CreateShareRemoteOperation("/1/",
                ShareType.PUBLIC_LINK,
                "",
                false,
                "",
                1).execute(client).isSuccess());

        assertTrue(new CreateFolderRemoteOperation("/2/", true).execute(nextcloudClient).isSuccess());
        assertTrue(new CreateShareRemoteOperation("/2/",
                ShareType.PUBLIC_LINK,
                "",
                false,
                "",
                1).execute(client).isSuccess());

        RemoteOperationResult<List<OCShare>> resultAll = new GetSharesRemoteOperation().execute(client);
        assertTrue(resultAll.isSuccess());
        assertEquals(2, resultAll.getResultData().size());
        assertEquals("/1/", resultAll.getResultData().get(0).getPath());
        assertEquals("/2/", resultAll.getResultData().get(1).getPath());

        RemoteOperationResult<List<OCShare>> result0 = new GetSharesForFileRemoteOperation("/0/", true, false).execute(client);
        assertTrue(result0.isSuccess());
        assertEquals(0, result0.getResultData().size());
        
        RemoteOperationResult<List<OCShare>> result2 = new GetSharesForFileRemoteOperation("/1/", true, false).execute(client);
        assertTrue(result2.isSuccess());
        assertEquals(1, result2.getResultData().size());
    }
}
