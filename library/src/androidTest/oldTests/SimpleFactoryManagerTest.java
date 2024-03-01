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
import com.owncloud.android.lib.common.SimpleFactoryManager;
import com.owncloud.android.lib.testclient.R;

import junit.framework.AssertionFailedError;

/**
 * Unit test for SimpleFactoryManager
 *
 * @author David A. Velasco
 */

public class SimpleFactoryManagerTest extends AndroidTestCase {

    private SimpleFactoryManager mSFMgr;

    private OwnCloudAccount mValidAccount;
    private OwnCloudAccount mAnonymousAccount;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mSFMgr = new SimpleFactoryManager();
        Uri serverUri = Uri.parse(getContext().getString(R.string.server_base_url));
        String username = getContext().getString(R.string.username);

        mValidAccount = new OwnCloudAccount(serverUri, OwnCloudCredentialsFactory.newBasicCredentials(
                username, getContext().getString(R.string.password)));

        mAnonymousAccount = new OwnCloudAccount(serverUri, OwnCloudCredentialsFactory.getAnonymousCredentials());
    }

    public void testGetClientFor() {
        try {
            OwnCloudClient client = mSFMgr.getClientFor(mValidAccount, getContext());

            assertNotSame("Got same client instances for same account", client, mSFMgr.getClientFor(mValidAccount,
                    getContext()));

            assertNotSame("Got same client instances for different accounts", client,
                    mSFMgr.getClientFor(mAnonymousAccount, getContext()));

        } catch (Exception e) {
            throw new AssertionFailedError("Exception getting client for account: " + e.getMessage());
        }
        // TODO harder tests
    }

    public void testRemoveClientFor() {
        try {
            OwnCloudClient client = mSFMgr.getClientFor(mValidAccount, getContext());
            mSFMgr.removeClientFor(mValidAccount);
            assertNotSame("Got same client instance after removing it from manager",
                    client, mSFMgr.getClientFor(mValidAccount, getContext()));
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
