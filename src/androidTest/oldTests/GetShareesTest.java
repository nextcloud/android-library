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

import android.util.Log;

import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.shares.GetRemoteShareesOperation;
import com.owncloud.android.lib.resources.shares.ShareType;
import com.owncloud.android.lib.testclient.R;

import junit.framework.AssertionFailedError;

import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class to test GetRemoteShareesOperation
 * <p>
 * With this TestCase we are experimenting a bit to improve the test suite design, in two aspects:
 * <p>
 * - Reduce the dependency from the set of test cases on the "test project" needed to
 * have an instrumented APK to install in the device, as required by the testing framework
 * provided by Android. To get there, this class avoids calling TestActivity methods in the test
 * method.
 * <p>
 * - Reduce the impact of creating a remote fixture over the Internet, while the structure of the
 * TestCase is kept easy to maintain. To get this, all the tests are done in a single test method,
 * granting this way that setUp and tearDown are run only once.
 */

public class GetShareesTest extends RemoteTest {

    private static final String LOG_TAG = GetShareesTest.class.getCanonicalName();

    /**
     * Test get sharees
     * <p>
     * Requires OC server 8.2 or later
     */
    public void testGetRemoteShareesOperation() {
        Log.v(LOG_TAG, "testGetRemoteSharees in");

        /// successful cases

        // search for sharees including "a"
        RemoteOperationResult result = new GetRemoteShareesOperation("a", 1, 50).execute(mClient);
        JSONObject resultItem;
        JSONObject value;
        int type;
        int userCount = 0;
        int groupCount = 0;
        assertTrue(result.isSuccess() && result.getData().size() > 0);
        try {
            for (int i = 0; i < result.getData().size(); i++) {
                resultItem = (JSONObject) result.getData().get(i);
                value = resultItem.getJSONObject(GetRemoteShareesOperation.NODE_VALUE);
                type = value.getInt(GetRemoteShareesOperation.PROPERTY_SHARE_TYPE);
                if (type == ShareType.GROUP.getValue()) {
                    groupCount++;
                } else {
                    userCount++;
                }
            }
            assertTrue(userCount > 0);
            assertTrue(groupCount > 0);
        } catch (JSONException e) {
            AssertionFailedError afe = new AssertionFailedError(e.getLocalizedMessage());
            afe.setStackTrace(e.getStackTrace());
            throw afe;
        }

        // search for sharees including "ad" - expecting user "admin" & group "admin"
        result = new GetRemoteShareesOperation("ad", 1, 50).execute(mClient);
        assertTrue(result.isSuccess() && result.getData().size() == 2);
        userCount = 0;
        groupCount = 0;
        try {
            for (int i = 0; i < 2; i++) {
                resultItem = (JSONObject) result.getData().get(i);
                value = resultItem.getJSONObject(GetRemoteShareesOperation.NODE_VALUE);
                type = value.getInt(GetRemoteShareesOperation.PROPERTY_SHARE_TYPE);
                if (type == ShareType.GROUP.getValue()) {
                    groupCount++;
                } else {
                    userCount++;
                }
            }
            assertEquals(userCount, 1);
            assertEquals(groupCount, 1);
        } catch (JSONException e) {
            AssertionFailedError afe = new AssertionFailedError(e.getLocalizedMessage());
            afe.setStackTrace(e.getStackTrace());
            throw afe;
        }


        // search for sharees including "bd" - expecting 0 results
        result = new GetRemoteShareesOperation("bd", 1, 50).execute(mClient);
        assertTrue(result.isSuccess() && result.getData().size() == 0);


        /// failed cases

        // search for sharees including wrong page values
        result = new GetRemoteShareesOperation("a", 0, 50).execute(mClient);
        assertTrue(!result.isSuccess() && result.getHttpCode() == HttpStatus.SC_BAD_REQUEST);

        result = new GetRemoteShareesOperation("a", 1, 0).execute(mClient);
        assertTrue(!result.isSuccess() && result.getHttpCode() == HttpStatus.SC_BAD_REQUEST);
    }

    /**
     * Test get federated sharees
     * <p>
     * Requires OC server 8.2 or later
     */
    public void testGetFederatedShareesOperation() {
        Log.v(LOG_TAG, "testGetFederatedSharees in");

        /// successful cases

        // search for sharees including "@"
        RemoteOperationResult result = new GetRemoteShareesOperation("@", 1, 50).execute(mClient);
        JSONObject resultItem;
        JSONObject value;
        int type;
        int fedCount = 0;
        assertTrue(result.isSuccess() && result.getData().size() > 0);
        try {
            for (int i = 0; i < result.getData().size(); i++) {
                resultItem = (JSONObject) result.getData().get(i);
                value = resultItem.getJSONObject(GetRemoteShareesOperation.NODE_VALUE);
                type = value.getInt(GetRemoteShareesOperation.PROPERTY_SHARE_TYPE);
                if (type == ShareType.FEDERATED.getValue()) {
                    fedCount++;
                }
            }
            assertTrue(fedCount > 0);
        } catch (JSONException e) {
            AssertionFailedError afe = new AssertionFailedError(e.getLocalizedMessage());
            afe.setStackTrace(e.getStackTrace());
            throw afe;
        }

        // search for 'admin' sharee from external server - expecting at least 1 result
        mServerUri2 = getActivity().getString(R.string.server_base_url_2);
        String remoteSharee = "admin@" + mServerUri2.split("//")[1];
        result = new GetRemoteShareesOperation(remoteSharee, 1, 50).execute(mClient);
        assertTrue(result.isSuccess() && result.getData().size() > 0);


        /// failed cases

        // search for sharees including wrong page values
        result = new GetRemoteShareesOperation("@", 0, 50).execute(mClient);
        assertTrue(!result.isSuccess() && result.getHttpCode() == HttpStatus.SC_BAD_REQUEST);

        result = new GetRemoteShareesOperation("@", 1, 0).execute(mClient);
        assertTrue(!result.isSuccess() && result.getHttpCode() == HttpStatus.SC_BAD_REQUEST);
    }

    @Override
    protected void tearDown() throws Exception {
        Log.v(LOG_TAG, "Deleting remote fixture...");
        super.tearDown();
        Log.v(LOG_TAG, "Remote fixture delete.");
    }
}
