/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.notifications;

import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.Utf8PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.net.HttpURLConnection;

public class RegisterAccountDeviceForProxyOperation {
    private static final String PROXY_ROUTE = "/devices";

    private static final String TAG = RegisterAccountDeviceForProxyOperation.class.getSimpleName();

    private final String proxyUrl;
    private final String pushToken;
    private final String deviceIdentifier;
    private final String deviceIdentifierSignature;
    private final String userPublicKey;
    private final String userAgent;

    private static final String PUSH_TOKEN = "pushToken";
    private static final String DEVICE_IDENTIFIER = "deviceIdentifier";
    private static final String DEVICE_IDENTIFIER_SIGNATURE = "deviceIdentifierSignature";
    private static final String USER_PUBLIC_KEY = "userPublicKey";

    public RegisterAccountDeviceForProxyOperation(String proxyUrl,
                                                  String pushToken,
                                                  String deviceIdentifier,
                                                  String deviceIdentifierSignature,
                                                  String userPublicKey,
                                                  String userAgent) {
        this.proxyUrl = proxyUrl;
        this.pushToken = pushToken;
        this.deviceIdentifier = deviceIdentifier;
        this.deviceIdentifierSignature = deviceIdentifierSignature;
        this.userPublicKey = userPublicKey;
        this.userAgent = userAgent;
    }

    public RemoteOperationResult run() {
        RemoteOperationResult result;
        int status;
        Utf8PostMethod post = null;

        try {
            // Post Method
            post = new Utf8PostMethod(proxyUrl + PROXY_ROUTE);
            post.setParameter(PUSH_TOKEN, pushToken);
            post.setParameter(DEVICE_IDENTIFIER, deviceIdentifier);
            post.setParameter(DEVICE_IDENTIFIER_SIGNATURE, deviceIdentifierSignature);
            post.setParameter(USER_PUBLIC_KEY, userPublicKey);
            post.setParameter(HttpMethodParams.USER_AGENT, userAgent);
            
            status = new HttpClient().executeMethod(post);
            String response = post.getResponseBodyAsString();

            if(isSuccess(status)) {
                result = new RemoteOperationResult(true, status, post.getResponseHeaders());
                Log_OC.d(TAG, "Successful response: " + response);
            } else {
                result = new RemoteOperationResult(false, status, post.getResponseHeaders());
            }

        } catch (Exception e) {
            result = new RemoteOperationResult(e);
            Log_OC.e(TAG, "Exception while registering device for notifications", e);

        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }
        return result;
    }

    private boolean isSuccess(int status) {
        return (status == HttpURLConnection.HTTP_OK);
    }
}
