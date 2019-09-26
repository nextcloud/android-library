/* Nextcloud Android Library is available under MIT license
 *
 *   @author Andy Scherzinger
 *   @author Mario Danic
 *   Copyright (C) 2017 Andy Scherzinger
 *   Copyright (C) 2017 Mario Danic
 *   Copyright (C) 2017 Nextcloud GmbH
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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.notifications.models.Notification;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Provides the remote notifications from the server handling the following data structure
 * accessible via the notifications endpoint at {@value OCS_ROUTE_LIST_V12_AND_UP}, specified at
 * {@link "https://github.com/nextcloud/notifications/blob/master/docs/ocs-endpoint-v2.md"}.
 */
public class GetNotificationsRemoteOperation extends RemoteOperation {

    // OCS Route
    private static final String OCS_ROUTE_LIST_V12_AND_UP =
            "/ocs/v2.php/apps/notifications/api/v2/notifications?format=json";

    private static final String TAG = GetNotificationsRemoteOperation.class.getSimpleName();

    // JSON Node names
    private static final String NODE_OCS = "ocs";

    private static final String NODE_DATA = "data";

    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result = null;
        int status = -1;
        GetMethod get = null;
        List<Notification> notifications;
        String url = client.getBaseUri() + OCS_ROUTE_LIST_V12_AND_UP;

        // get the notifications
        try {
            get = new GetMethod(url);
            get.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);

            status = client.executeMethod(get);
            String response = get.getResponseBodyAsString();

            if (isSuccess(status)) {
                result = new RemoteOperationResult(true, status, get.getResponseHeaders());
                Log_OC.d(TAG, "Successful response: " + response);

                // Parse the response
                notifications = parseResult(response);
                result.setNotificationData(notifications);
            } else {
                result = new RemoteOperationResult(false, status, get.getResponseHeaders());
                Log_OC.e(TAG, "Failed response while getting user notifications ");
                if (response != null) {
                    Log_OC.e(TAG, "*** status code: " + status + " ; response message: " + response);
                } else {
                    Log_OC.e(TAG, "*** status code: " + status);
                }
            }
        } catch (Exception e) {
            result = new RemoteOperationResult(e);
            Log_OC.e(TAG, "Exception while getting remote notifications", e);
        } finally {
            if (get != null) {
                get.releaseConnection();
            }
        }

        return result;
    }

    private List<Notification> parseResult(String response) throws JSONException {
        JsonParser jsonParser = new JsonParser();
        JsonObject jo = (JsonObject)jsonParser.parse(response);
        JsonArray jsonDataArray = jo.getAsJsonObject(NODE_OCS).getAsJsonArray(NODE_DATA);

        Gson gson = new Gson();
        Type listType = new TypeToken<List<Notification>>(){}.getType();

        return gson.fromJson(jsonDataArray, listType);
    }

    private boolean isSuccess(int status) {
        return (status == HttpStatus.SC_OK);
    }
}
