/*  Nextcloud Android Library is available under MIT license
 *   Copyright (C) 2017 Mario Danic
 *
 *   @author Mario Danic
 *
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
package com.owncloud.android.lib.resources.notifications;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.LegacyRemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.notifications.models.PushResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.Utf8PostMethod;

import java.lang.reflect.Type;

public class RegisterAccountDeviceForNotificationsOperation extends LegacyRemoteOperation<PushResponse> {
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
