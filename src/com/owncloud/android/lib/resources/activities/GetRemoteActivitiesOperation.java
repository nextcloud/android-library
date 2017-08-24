/**  Nextcloud Android Library is available under MIT license
 *
 *   Copyright (C) 2017 Alejandro Bautista
 *   @author Alejandro Bautista
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

package com.owncloud.android.lib.resources.activities;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.activities.models.Activity;
import com.owncloud.android.lib.resources.activities.models.RichElement;
import com.owncloud.android.lib.resources.activities.models.RichElementTypeAdapter;
import com.owncloud.android.lib.resources.status.OwnCloudVersion;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides the remote activities from the server handling the following data structure
 * accessible via the activities endpoint at {@value OCS_ROUTE_V12_AND_UP}, specified at
 * {@link "https://github.com/nextcloud/activity/blob/master/docs/endpoint-v2.md"}.
 */
public class GetRemoteActivitiesOperation extends RemoteOperation{

    private static final String TAG = GetRemoteActivitiesOperation.class.getSimpleName();

    // OCS Routes
    private static final String OCS_ROUTE_V12_AND_UP = "/ocs/v2.php/apps/activity/api/v2/activity?format=json";
    private static final String OCS_ROUTE_PRE_V12 = "/ocs/v1.php/cloud/activity?format=json";

    // JSON Node names
    private static final String NODE_OCS = "ocs";

    private static final String NODE_DATA = "data";

    private String nextUrl = "";

    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result = null;
        int status = -1;
        GetMethod get = null;
        ArrayList<Object> activities;
        String url;
        if (nextUrl.isEmpty()) {
            if (client.getOwnCloudVersion().compareTo(OwnCloudVersion.nextcloud_12) >= 0) {
                url = client.getBaseUri() + OCS_ROUTE_V12_AND_UP;
            } else {
                url = client.getBaseUri() + OCS_ROUTE_PRE_V12;
            }
        } else {
            url = nextUrl;
        }
        Log_OC.d(TAG, "URL: " + url);

        try {
            get = new GetMethod(url);
            get.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);

            status = client.executeMethod(get);
            String response = get.getResponseBodyAsString();

            Header nextPageHeader = get.getResponseHeader("Link");
            if (nextPageHeader != null) {
                String link = nextPageHeader.getValue();
                if (link.startsWith("<") && link.endsWith(">; rel=\"next\"")) {
                    nextUrl = nextPageHeader.getValue().substring(1, link.length() - 13);
                    Log_OC.d(TAG, "nextUrl");
                    Log_OC.d(TAG, nextUrl);
                } else {
                    nextUrl = "";
                }
            } else {
                nextUrl = "";
            }

            if (isSuccess(status)) {
                Log_OC.d(TAG, "Successful response: " + response);
                result = new RemoteOperationResult(true, status, get.getResponseHeaders());
                // Parse the response
                activities = parseResult(response);
                result.setData(activities);
            } else {
                result = new RemoteOperationResult(false, status, get.getResponseHeaders());
                Log_OC.e(TAG, "Failed response while getting user activities ");
                if (response != null) {
                    Log_OC.e(TAG, "*** status code: " + status + " ; response message: " + response);
                } else {
                    Log_OC.e(TAG, "*** status code: " + status);
                }
            }
        } catch (Exception e) {
            result = new RemoteOperationResult(e);
            Log_OC.e(TAG, "Exception while getting remote activities", e);
        } finally {
            if (get != null) {
                get.releaseConnection();
            }
        }

        return result;
    }

    public Boolean hasMoreActivities() {
        return !nextUrl.isEmpty();
    }

    private ArrayList<Object> parseResult(String response) throws JSONException {
        JsonParser jsonParser = new JsonParser();
        JsonObject jo = (JsonObject)jsonParser.parse(response);
        JsonArray jsonDataArray = jo.getAsJsonObject(NODE_OCS).getAsJsonArray(NODE_DATA);

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(RichElement.class,new RichElementTypeAdapter())//Add TypeAdapter to parse RichElement
                .create();
        Type listType = new TypeToken<List<Activity>>(){}.getType();

        return gson.fromJson(jsonDataArray, listType);
    }

    private boolean isSuccess(int status) {
        return (status == HttpStatus.SC_OK);
    }
}
