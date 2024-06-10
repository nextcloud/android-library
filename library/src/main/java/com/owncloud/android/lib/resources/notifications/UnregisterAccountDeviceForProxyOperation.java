/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.notifications;

import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.HttpDeleteWithBody;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpClient;

import java.net.HttpURLConnection;

public class UnregisterAccountDeviceForProxyOperation {
    private static final String PROXY_ROUTE = "/devices";

    private static final String TAG = RegisterAccountDeviceForProxyOperation.class.getSimpleName();

    private String proxyUrl;
    private String deviceIdentifier;
    private String deviceIdentifierSignature;
    private String userPublicKey;

    private static final String DEVICE_IDENTIFIER = "deviceIdentifier";
    private static final String DEVICE_IDENTIFIER_SIGNATURE = "deviceIdentifierSignature";
    private static final String USER_PUBLIC_KEY = "userPublicKey";

    public UnregisterAccountDeviceForProxyOperation(String proxyUrl,
                                                    String deviceIdentifier,
                                                    String deviceIdentifierSignature,
                                                    String userPublicKey) {
        this.proxyUrl = proxyUrl;
        this.deviceIdentifier = deviceIdentifier;
        this.deviceIdentifierSignature = deviceIdentifierSignature;
        this.userPublicKey = userPublicKey;
    }

    public RemoteOperationResult run() {
        RemoteOperationResult result;
        int status;
        HttpDeleteWithBody delete = null;

        try {
            // Post Method
            delete = new HttpDeleteWithBody(proxyUrl + PROXY_ROUTE);
            delete.setParameter(DEVICE_IDENTIFIER, deviceIdentifier);
            delete.setParameter(DEVICE_IDENTIFIER_SIGNATURE, deviceIdentifierSignature);
            delete.setParameter(USER_PUBLIC_KEY, userPublicKey);

            status = new HttpClient().executeMethod(delete);
            String response = delete.getResponseBodyAsString();

            if(isSuccess(status)) {
                result = new RemoteOperationResult(true, status, delete.getResponseHeaders());
                Log_OC.d(TAG, "Successful response: " + response);
            } else {
                result = new RemoteOperationResult(false, status, delete.getResponseHeaders());
            }

        } catch (Exception e) {
            result = new RemoteOperationResult(e);
            Log_OC.e(TAG, "Exception while registering device for notifications", e);

        } finally {
            if (delete != null) {
                delete.releaseConnection();
            }
        }
        return result;
    }

    private boolean isSuccess(int status) {
        return (status == HttpURLConnection.HTTP_OK);
    }
}
