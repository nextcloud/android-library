/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.notifications;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.notifications.models.PushResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.Utf8PostMethod;

import java.lang.reflect.Type;

public class RegisterAccountDeviceForNotificationsOperation extends RemoteOperation<PushResponse> {
    // OCS Route
    private static final String OCS_ROUTE =
            "/ocs/v2.php/apps/notifications/api/v2/push" + JSON_FORMAT;

    private static final String TAG = RegisterAccountDeviceForNotificationsOperation.class.getSimpleName();

    // JSON Node names
    private static final String NODE_OCS = "ocs";
    private static final String NODE_DATA = "data";
    private static final String MESSAGE = "message";

    private static final String PUSH_TOKEN_HASH = "pushTokenHash";
    private static final String DEVICE_PUBLIC_KEY = "devicePublicKey";
    private static final String PROXY_SERVER = "proxyServer";
    private static final String INVALID_SESSION_TOKEN = "INVALID_SESSION_TOKEN";

    private final String pushTokenHash;
    private final String devicePublicKey;
    private final String proxyServer;

    public RegisterAccountDeviceForNotificationsOperation(String pushTokenHash,
                                                          String devicePublicKey,
                                                          String proxyServer) {
        this.pushTokenHash = pushTokenHash;
        this.devicePublicKey = devicePublicKey;
        this.proxyServer = proxyServer;
    }

    @Override
    protected RemoteOperationResult<PushResponse> run(OwnCloudClient client) {
        RemoteOperationResult<PushResponse> result;
        int status;
        Utf8PostMethod post = null;

        try {
            // Post Method
            post = new Utf8PostMethod(client.getBaseUri() + OCS_ROUTE);
            post.setParameter(PUSH_TOKEN_HASH, pushTokenHash);
            post.setParameter(DEVICE_PUBLIC_KEY, devicePublicKey);
            post.setParameter(PROXY_SERVER, proxyServer);

            post.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);

            status = client.executeMethod(post);
            String response = post.getResponseBodyAsString();

            if (isSuccess(status)) {
                result = new RemoteOperationResult<>(true, status, post.getResponseHeaders());
                Log_OC.d(TAG, "Successful response: " + response);

                // Parse the response
                PushResponse pushResponse = parseResult(response);
                result.setResultData(pushResponse);
            } else {
                if (isInvalidSessionToken(response)) {
                    result = new RemoteOperationResult<>(RemoteOperationResult.ResultCode.ACCOUNT_USES_STANDARD_PASSWORD);
                } else {
                    result = new RemoteOperationResult<>(false, status, post.getResponseHeaders());
                }
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Exception while registering device for notifications", e);
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }
        return result;
    }

    private PushResponse parseResult(String response) {
        JsonObject jo = (JsonObject) JsonParser.parseString(response);
        JsonObject jsonDataObject = jo.getAsJsonObject(NODE_OCS).getAsJsonObject(NODE_DATA);

        Gson gson = new Gson();
        Type pushResponseType = new TypeToken<PushResponse>() {
        }.getType();

        return gson.fromJson(jsonDataObject, pushResponseType);
    }

    private boolean isInvalidSessionToken(String response) {
        JsonObject jsonObject = (JsonObject) JsonParser.parseString(response);
        String message = jsonObject.getAsJsonObject(NODE_OCS).getAsJsonObject(NODE_DATA).get(MESSAGE).getAsString();

        return INVALID_SESSION_TOKEN.equals(message);
    }

    private boolean isSuccess(int status) {
        return (status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED);
    }

}
