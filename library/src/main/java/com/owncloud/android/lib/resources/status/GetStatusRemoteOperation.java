/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2016-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2016 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-FileCopyrightText: 2014 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-FileCopyrightText: 2014 masensio <masensio@solidgear.es>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.status;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.GetMethod;
import com.owncloud.android.lib.common.accounts.AccountUtils;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import kotlin.Pair;

/**
 * Checks if the server is valid and if the server supports the Share API
 *
 * @author David A. Velasco
 * @author masensio
 */
public class GetStatusRemoteOperation extends RemoteOperation<Pair<OwnCloudVersion, Boolean>> {
    private static final String TAG = GetStatusRemoteOperation.class.getSimpleName();

    private static final String NODE_INSTALLED = "installed";
    private static final String NODE_VERSION = "version";
    private static final String NODE_EXTENDED_SUPPORT = "extendedSupport";
    private static final String PROTOCOL_HTTPS = "https://";
    private static final String PROTOCOL_HTTP = "http://";
    private static final int UNTRUSTED_DOMAIN_ERROR_CODE = 15;

    private RemoteOperationResult<Pair<OwnCloudVersion, Boolean>> mLatestResult;
    private final Context mContext;

    public GetStatusRemoteOperation(Context context) {
        mContext = context;
    }

    private boolean tryConnection(NextcloudClient client) {
        boolean retval = false;
        com.nextcloud.operations.GetMethod get = null;
        String baseUrlSt = String.valueOf(client.getBaseUri());
        try {
            get = new com.nextcloud.operations.GetMethod(baseUrlSt + AccountUtils.STATUS_PATH, false);

            client.setFollowRedirects(false);
            boolean isRedirectToNonSecureConnection = false;
            int status = client.execute(get);
            mLatestResult = new RemoteOperationResult<>((status == HttpStatus.SC_OK), get);

            String redirectedLocation = mLatestResult.getRedirectedLocation();
            while (redirectedLocation != null && redirectedLocation.length() > 0
                    && !mLatestResult.isSuccess()) {

                isRedirectToNonSecureConnection |= (
                        baseUrlSt.startsWith(PROTOCOL_HTTPS) &&
                                redirectedLocation.startsWith(PROTOCOL_HTTP)
                );
                get.releaseConnection();
                get = new GetMethod(redirectedLocation, false);
                status = client.execute(get);
                mLatestResult = new RemoteOperationResult<>((status == HttpStatus.SC_OK), get);
                redirectedLocation = mLatestResult.getRedirectedLocation();
            }

            String response = get.getResponseBodyAsString();

            if (status == HttpStatus.SC_OK) {
                JSONObject json = new JSONObject(response);
                if (!json.getBoolean(NODE_INSTALLED)) {
                    mLatestResult = new RemoteOperationResult<>(
                            RemoteOperationResult.ResultCode.INSTANCE_NOT_CONFIGURED);
                } else {
                    boolean extendedSupport = false;
                    if (json.has(NODE_EXTENDED_SUPPORT)) {
                        extendedSupport = json.getBoolean(NODE_EXTENDED_SUPPORT);
                    }

                    String version = json.getString(NODE_VERSION);
                    OwnCloudVersion ocVersion = new OwnCloudVersion(version);

                    if (!ocVersion.isVersionValid()) {
                        mLatestResult = new RemoteOperationResult<>(RemoteOperationResult.ResultCode.BAD_OC_VERSION);
                    } else {
                        // success
                        if (isRedirectToNonSecureConnection) {
                            mLatestResult = new RemoteOperationResult<>(
                                    RemoteOperationResult.ResultCode.OK_REDIRECT_TO_NON_SECURE_CONNECTION);
                        } else {
                            mLatestResult = new RemoteOperationResult<>(
                                    baseUrlSt.startsWith(PROTOCOL_HTTPS) ?
                                            RemoteOperationResult.ResultCode.OK_SSL :
                                            RemoteOperationResult.ResultCode.OK_NO_SSL
                            );
                        }

                        Pair<OwnCloudVersion, Boolean> data = new Pair<>(ocVersion, extendedSupport);
                        mLatestResult.setResultData(data);
                        retval = true;
                    }
                }
            } else if (status == HttpStatus.SC_BAD_REQUEST) {
                try {
                    JSONObject json = new JSONObject(response);

                    if (json.getInt("code") == UNTRUSTED_DOMAIN_ERROR_CODE) {
                        mLatestResult = new RemoteOperationResult<>(RemoteOperationResult.ResultCode.UNTRUSTED_DOMAIN);
                    } else {
                        mLatestResult = new RemoteOperationResult<>(false, get);
                    }
                } catch (JSONException e) {
                    mLatestResult = new RemoteOperationResult<>(false, get);
                }
            } else {
                mLatestResult = new RemoteOperationResult<>(false, get);
            }

        } catch (JSONException e) {
            mLatestResult = new RemoteOperationResult<>(RemoteOperationResult.ResultCode.INSTANCE_NOT_CONFIGURED);

        } catch (Exception e) {
            mLatestResult = new RemoteOperationResult<>(e);

        } finally {
            if (get != null)
                get.releaseConnection();
        }

        if (mLatestResult.isSuccess()) {
            Log_OC.i(TAG, "Connection check at " + baseUrlSt + ": " + mLatestResult.getLogMessage());

        } else if (mLatestResult.getException() != null) {
            Log_OC.e(TAG, "Connection check at " + baseUrlSt + ": " + mLatestResult.getLogMessage(),
                    mLatestResult.getException());

        } else {
            Log_OC.e(TAG, "Connection check at " + baseUrlSt + ": " + mLatestResult.getLogMessage());
        }

        return retval;
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    @Override
    public RemoteOperationResult<Pair<OwnCloudVersion, Boolean>> run(NextcloudClient client) {
        if (!isOnline()) {
            return new RemoteOperationResult<>(RemoteOperationResult.ResultCode.NO_NETWORK_CONNECTION);
        }
        String baseUriStr = String.valueOf(client.getBaseUri());
        if (baseUriStr.startsWith(PROTOCOL_HTTP) || baseUriStr.startsWith(PROTOCOL_HTTPS)) {
            tryConnection(client);

        } else {
            client.setBaseUri(Uri.parse(PROTOCOL_HTTPS + baseUriStr));
            boolean httpsSuccess = tryConnection(client);
            if (!httpsSuccess && !mLatestResult.isSslRecoverableException()) {
                Log_OC.d(TAG, "establishing secure connection failed, trying non secure connection");
                client.setBaseUri(Uri.parse(PROTOCOL_HTTP + baseUriStr));
                tryConnection(client);
            }
        }
        return mLatestResult;
    }

}
