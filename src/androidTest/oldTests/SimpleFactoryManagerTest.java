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
