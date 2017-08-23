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


import android.net.Uri;

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
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONException;

import java.lang.reflect.Type;

public class RegisterAccountDeviceForNotificationsOperation extends RemoteOperation {
    // OCS Route
    private static final String OCS_ROUTE =
            "/ocs/v2.php/apps/notifications/api/v2/push?format=json";

    private static final String TAG = RegisterAccountDeviceForNotificationsOperation.class.getSimpleName();

    // JSON Node names
    private static final String NODE_OCS = "ocs";
    private static final String NODE_DATA = "data";

    private static final String PUSH_TOKEN_HASH = "pushTokenHash";
    private static final String DEVICE_PUBLIC_KEY = "devicePublicKey";
    private static final String PROXY_SERVER = "proxyServer";

    private String pushTokenHash;
    private String devicePublicKey;
    private String proxyServer;

    public RegisterAccountDeviceForNotificationsOperation(String pushTokenHash, String devicePublicKey,
                                                          String proxyServer) {
        this.pushTokenHash = pushTokenHash;
        this.devicePublicKey = devicePublicKey;
        this.proxyServer = proxyServer;
    }

    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result = null;
        int status = -1;
        PushResponse pushResponse;
        PostMethod post = null;

        try {
            // Post Method
            String uri = Uri.parse(client.getBaseUri() + OCS_ROUTE)
                    .buildUpon()
                    .appendQueryParameter(PUSH_TOKEN_HASH, pushTokenHash)
                    .appendQueryParameter(DEVICE_PUBLIC_KEY, devicePublicKey)
                    .appendQueryParameter(PROXY_SERVER, proxyServer)
                    .build().toString();

            post = new PostMethod(uri);
            post.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);
            
            status = client.executeMethod(post);
            String response = post.getResponseBodyAsString();

            if(isSuccess(status)) {
                result = new RemoteOperationResult(true, status, post.getResponseHeaders());
                Log_OC.d(TAG, "Successful response: " + response);

                // Parse the response
                pushResponse = parseResult(response);
                result.setPushResponseData(pushResponse);
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

    private PushResponse parseResult(String response) throws JSONException {
        JsonParser jsonParser = new JsonParser();
        JsonObject jo = (JsonObject)jsonParser.parse(response);
        JsonObject jsonDataObject = jo.getAsJsonObject(NODE_OCS).getAsJsonObject(NODE_DATA);

        Gson gson = new Gson();
        Type pushResponseType = new TypeToken<PushResponse>(){}.getType();

        return gson.fromJson(jsonDataObject, pushResponseType);
    }

    private boolean isSuccess(int status) {
        return (status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED);
    }

    private String assembleJson() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        stringBuilder.append("\"");
        stringBuilder.append(PUSH_TOKEN_HASH);
        stringBuilder.append("\"");
        stringBuilder.append(":\"");
        stringBuilder.append(pushTokenHash.trim());
        stringBuilder.append("\",");
        stringBuilder.append("\"");
        stringBuilder.append(DEVICE_PUBLIC_KEY);
        stringBuilder.append("\"");
        stringBuilder.append(":\"");
        stringBuilder.append(devicePublicKey.trim());
        stringBuilder.append("\",");
        stringBuilder.append("\"");
        stringBuilder.append(PROXY_SERVER);
        stringBuilder.append("\"");
        stringBuilder.append(":\"");
        stringBuilder.append(proxyServer.trim());
        stringBuilder.append("\"");
        stringBuilder.append("}");

        return stringBuilder.toString();
    }

}
