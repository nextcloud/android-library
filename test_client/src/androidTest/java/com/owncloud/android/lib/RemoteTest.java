/* ownCloud Android Library is available under MIT license
 *   Copyright (C) 2015 ownCloud Inc.
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
package com.owncloud.android.lib;

import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.network.NetworkUtils;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.testclient.R;
import com.owncloud.android.lib.testclient.SelfSignedConfidentSslSocketFactory;
import com.owncloud.android.lib.testclient.TestActivity;

import junit.framework.AssertionFailedError;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

/**
 * Class to create test folder for further testing
 *
 * @author David A. Velasco
 */
public class RemoteTest extends ActivityInstrumentationTestCase2<TestActivity> {

    private static final String LOG_TAG = RemoteTest.class.getSimpleName();

    protected String mBaseFolderPath = "/test_for_build_";
    protected TestActivity mActivity;
    protected OwnCloudClient mClient;
    protected String mServerUri2;
    private String mServerUri;
    private String mUser;
    private String mPass;

    public RemoteTest() {
        super(TestActivity.class);

        Protocol pr = Protocol.getProtocol("https");
        if (pr == null || !(pr.getSocketFactory() instanceof SelfSignedConfidentSslSocketFactory)) {
            try {
                ProtocolSocketFactory psf = new SelfSignedConfidentSslSocketFactory();
                Protocol.registerProtocol("https", new Protocol("https", psf, 443));
            } catch (GeneralSecurityException e) {
                throw new AssertionFailedError("Self-signed confident SSL context could not be loaded");
            }
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        mBaseFolderPath += Long.toString(System.currentTimeMillis());

        RemoteOperationResult result = getActivity().createFolder(mBaseFolderPath, true);
        if (!result.isSuccess()) {
            Utils.logAndThrow(LOG_TAG, result);
        }

        mActivity = getActivity();

        mServerUri = getActivity().getString(R.string.server_base_url);
        mServerUri2 = getActivity().getString(R.string.server_base_url_2);
        mUser = getActivity().getString(R.string.username);
        mPass = getActivity().getString(R.string.password);

        initAccessToServer();
    }

    private void initAccessToServer() {
        Log_OC.v(LOG_TAG, "Setting up client instance to access OC server...");

        mClient = new OwnCloudClient(Uri.parse(mServerUri), NetworkUtils.getMultiThreadedConnManager(), false);
        mClient.setDefaultTimeouts(OwnCloudClientFactory.DEFAULT_DATA_TIMEOUT,
                OwnCloudClientFactory.DEFAULT_CONNECTION_TIMEOUT);
        mClient.setFollowRedirects(true);
        mClient.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials(mUser, mPass));

        Log_OC.v(LOG_TAG, "Client instance set up.");
    }


    @Override
    protected void tearDown() throws Exception {
        RemoteOperationResult removeResult = getActivity().removeFile(mBaseFolderPath);
        if (!removeResult.isSuccess()) {
            Utils.logAndThrow(LOG_TAG, removeResult);
        }
        super.tearDown();
    }

    public File getFile(String filename) throws IOException {
        InputStream inputStream = getInstrumentation().getContext().getAssets().open(filename);
        File temp = File.createTempFile("file", "file");
        FileUtils.copyInputStreamToFile(inputStream, temp);

        return temp;
    }

}
