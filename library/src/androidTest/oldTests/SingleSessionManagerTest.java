/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android;

import android.net.Uri;
import android.test.AndroidTestCase;

import com.owncloud.android.lib.common.OwnCloudAccount;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.SingleSessionManager;
import com.owncloud.android.lib.testclient.R;

import junit.framework.AssertionFailedError;

/**
 * Unit test for SingleSessionManager
 *
 * @author David A. Velasco
 */
public class SingleSessionManagerTest extends AndroidTestCase {

    private SingleSessionManager mSSMgr;

    private OwnCloudAccount mValidAccount;
    private OwnCloudAccount mAnonymousAccount;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mSSMgr = new SingleSessionManager();
        Uri serverUri = Uri.parse(getContext().getString(R.string.server_base_url));
        String username = getContext().getString(R.string.username);

        mValidAccount = new OwnCloudAccount(serverUri, OwnCloudCredentialsFactory.newBasicCredentials(username,
                getContext().getString(R.string.password)));

        mAnonymousAccount = new OwnCloudAccount(serverUri, OwnCloudCredentialsFactory.getAnonymousCredentials());
    }

    public void testGetClientFor() {
        try {
            OwnCloudClient client1 = mSSMgr.getClientFor(mValidAccount, getContext());
            OwnCloudClient client2 = mSSMgr.getClientFor(mAnonymousAccount, getContext());

            assertNotSame("Got same client instances for different accounts", client1, client2);
            assertSame("Got different client instances for same account",
                    client1, mSSMgr.getClientFor(mValidAccount, getContext()));
        } catch (Exception e) {
            throw new AssertionFailedError("Exception getting client for account: " + e.getMessage());
        }

        // TODO harder tests
    }

    public void testRemoveClientFor() {
        try {
            OwnCloudClient client1 = mSSMgr.getClientFor(mValidAccount, getContext());
            mSSMgr.removeClientFor(mValidAccount);
            assertNotSame("Got same client instance after removing it from manager",
                    client1, mSSMgr.getClientFor(mValidAccount, getContext()));
        } catch (Exception e) {
            throw new AssertionFailedError("Exception getting client for account: " + e.getMessage());
        }

        // TODO harder tests
    }


//    public void testSaveAllClients() {
        // TODO implement test;
        // 		or refactor saveAllClients() method out of OwnCloudClientManager to make 
        //		it independent of AccountManager
//    }
}
