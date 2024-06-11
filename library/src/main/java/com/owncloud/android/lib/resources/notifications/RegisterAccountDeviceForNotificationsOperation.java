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
import com.nextcloud.common.JSONRequestBody;
import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.PostMethod;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.notifications.models.PushResponse;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;

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
    public RemoteOperationResult<PushResponse> run(NextcloudClient client) {
        RemoteOperationResult<PushResponse> result;
        int status;
        PostMethod post = null;

        try {
            // request body
            JSONRequestBody jsonRequestBody = new JSONRequestBody(PUSH_TOKEN_HASH, pushTokenHash);
            jsonRequestBody.put(DEVICE_PUBLIC_KEY, devicePublicKey);
            jsonRequestBody.put(PROXY_SERVER, proxyServer);

            // Post Method
            post = new PostMethod(client.getBaseUri() + OCS_ROUTE, true, jsonRequestBody.get());

            status = client.execute(post);
            String response = post.getResponseBodyAsString();

            if (isSuccess(status)) {
                result = new RemoteOperationResult<>(true, post);
                Log_OC.d(TAG, "Successful response: " + response);

                // Parse the response
                PushResponse pushResponse = parseResult(response);
                result.setResultData(pushResponse);
            } else {
                if (isInvalidSessionToken(response)) {
                    result = new RemoteOperationResult<>(RemoteOperationResult.ResultCode.ACCOUNT_USES_STANDARD_PASSWORD);
                } else {
                    result = new RemoteOperationResult<>(false, post);
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
        return (status == HttpURLConnection.HTTP_OK || status == HttpURLConnection.HTTP_CREATED);
    }

}
