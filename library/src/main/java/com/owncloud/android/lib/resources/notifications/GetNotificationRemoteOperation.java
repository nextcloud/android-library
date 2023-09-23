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

package com.owncloud.android.lib.resources.notifications;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.LegacyRemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.notifications.models.Notification;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import java.lang.reflect.Type;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Provides the remote notifications from the server handling the following data structure
 * accessible via the notifications endpoint at {@value OCS_ROUTE_LIST_V12_AND_UP}, specified at
 * {@link "https://github.com/nextcloud/notifications/blob/master/docs/ocs-endpoint-v2.md"}.
 */
public class GetNotificationRemoteOperation extends LegacyRemoteOperation<Notification> {

    // OCS Route
    private static final String OCS_ROUTE_LIST_V12_AND_UP =
            "/ocs/v2.php/apps/notifications/api/v2/notifications/";

    // JSON Node names
    private static final String NODE_OCS = "ocs";
    private static final String NODE_DATA = "data";

    private int id;

    public GetNotificationRemoteOperation(int id) {
        this.id = id;
    }

    @SuppressFBWarnings("HTTP_PARAMETER_POLLUTION")
    @Override
    protected RemoteOperationResult<Notification> run(OwnCloudClient client) {
        RemoteOperationResult<Notification> result;
        int status;
        GetMethod get = null;
        String url = client.getBaseUri() + OCS_ROUTE_LIST_V12_AND_UP + id + JSON_FORMAT;

        // get the notification
        try {
            get = new GetMethod(url);
            get.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);

            status = client.executeMethod(get);
            String response = get.getResponseBodyAsString();

            if (isSuccess(status)) {
                result = new RemoteOperationResult<>(true, status, get.getResponseHeaders());
                Log_OC.d(this, "Successful response: " + response);

                // Parse the response
                Notification notification = parseResult(response);
                result.setResultData(notification);
            } else {
                result = new RemoteOperationResult<>(false, status, get.getResponseHeaders());
                Log_OC.e(this, "Failed response while getting user notifications ");
                if (response != null) {
                    Log_OC.e(this, "*** status code: " + status + " ; response message: " + response);
                } else {
                    Log_OC.e(this, "*** status code: " + status);
                }
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(this, "Exception while getting remote notifications", e);
        } finally {
            if (get != null) {
                get.releaseConnection();
            }
        }

        return result;
    }

    private Notification parseResult(String response) {
        JsonObject jo = (JsonObject) JsonParser.parseString(response);
        JsonObject jsonDataObject = jo.getAsJsonObject(NODE_OCS).getAsJsonObject(NODE_DATA);

        Gson gson = new Gson();
        Type type = new TypeToken<Notification>() {
        }.getType();

        return gson.fromJson(jsonDataObject, type);
    }

    private boolean isSuccess(int status) {
        return (status == HttpStatus.SC_OK);
    }
}
