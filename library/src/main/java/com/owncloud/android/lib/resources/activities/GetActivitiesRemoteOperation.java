/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2018 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2017 Alejandro Bautista <aleister09@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.activities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.GetMethod;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.activities.model.Activity;
import com.owncloud.android.lib.resources.activities.model.RichElement;
import com.owncloud.android.lib.resources.activities.model.RichElementTypeAdapter;
import com.owncloud.android.lib.resources.activities.models.PreviewObject;
import com.owncloud.android.lib.resources.activities.models.PreviewObjectAdapter;

import org.apache.commons.httpclient.HttpStatus;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kotlin.Pair;

/**
 * Provides the remote activities from the server handling the following data structure
 * accessible via the activities endpoint at {@value OCS_ROUTE_V12_AND_UP}, specified at
 * {@link "https://github.com/nextcloud/activity/blob/master/docs/endpoint-v2.md"}.
 */
public class GetActivitiesRemoteOperation extends RemoteOperation<Pair<ArrayList<Activity>, Integer>> {

    private static final String TAG = GetActivitiesRemoteOperation.class.getSimpleName();

    // OCS Routes
    private static final String OCS_ROUTE_V12_AND_UP = "/ocs/v2.php/apps/activity/api/v2/activity";

    // JSON Node names
    private static final String NODE_OCS = "ocs";

    private static final String NODE_DATA = "data";

    private int lastGiven = -1;
    
    private long fileId = -1;

    public GetActivitiesRemoteOperation() {
    }

    public GetActivitiesRemoteOperation(long fileId) {
        this.fileId = fileId;
    }

    public GetActivitiesRemoteOperation(long fileId, int lastGiven) {
        this.fileId = fileId;
        this.lastGiven = lastGiven;
    }
    
    public GetActivitiesRemoteOperation(int lastGiven) {
        this.lastGiven = lastGiven;
    }

    @Override
    public RemoteOperationResult<Pair<ArrayList<Activity>, Integer>> run(NextcloudClient client) {
        RemoteOperationResult<Pair<ArrayList<Activity>, Integer>> result;
        int status;
        GetMethod get = null;
        ArrayList<Activity> activities;
        String url = client.getBaseUri() + OCS_ROUTE_V12_AND_UP;

        // add filter for fileId, if available
        if (fileId > 0) {
            url = url + "/filter";
        }

        Log_OC.d(TAG, "URL: " + url);

        try {
            get = new GetMethod(url, true);

            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("format", "json");
            parameters.put("previews", "true");

            if (lastGiven != -1) {
                parameters.put("since", String.valueOf(lastGiven));
            }

            if (fileId > 0) {
                parameters.put("sort", "desc");
                parameters.put("object_type", "files");
                parameters.put("object_id", String.valueOf(fileId));
            }

            get.setQueryString(parameters);

            status = client.execute(get);
            String response = get.getResponseBodyAsString();

            if (isSuccess(status)) {
                String nextPageHeader = get.getResponseHeader("X-Activity-Last-Given");
                if (nextPageHeader != null) {
                    lastGiven = Integer.parseInt(nextPageHeader);
                } else {
                    lastGiven = -1;
                }

                Log_OC.d(TAG, "Successful response: " + response);
                result = new RemoteOperationResult<>(true, get);
                // Parse the response
                activities = parseResult(response);

                result.setResultData(new Pair<>(activities, lastGiven));
            } else {
                result = new RemoteOperationResult<>(false, get);
                Log_OC.e(TAG, "Failed response while getting user activities");
                Log_OC.e(TAG, "*** status code: " + status + " ; response message: " + response);
            }
        } catch (IOException e) {
            Log_OC.e(TAG, "Error getting user activities", e);
            return new RemoteOperationResult<>(e);
        } finally {
            if (get != null) {
                get.releaseConnection();
            }
        }

        return result;
    }

    public boolean hasMoreActivities() {
        return lastGiven> 0;
    }

    protected ArrayList<Activity> parseResult(String response) {
        if (response == null || response.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            JsonParser jsonParser = new JsonParser();
            JsonObject jo = (JsonObject) jsonParser.parse(response);
            JsonArray jsonDataArray = jo.getAsJsonObject(NODE_OCS).getAsJsonArray(NODE_DATA);

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(RichElement.class, new RichElementTypeAdapter())
                    .registerTypeAdapter(PreviewObject.class, new PreviewObjectAdapter())
                    .create();
            Type listType = new TypeToken<List<Activity>>() {
            }.getType();

            return gson.fromJson(jsonDataArray, listType);

        } catch (JsonSyntaxException e) {
            Log_OC.e(TAG, "Not a valid json: " + response, e);
            return new ArrayList<>();
        }
    }

    private boolean isSuccess(int status) {
        return (status == HttpStatus.SC_OK || status == HttpStatus.SC_NOT_MODIFIED || status == HttpStatus.SC_NOT_FOUND);
    }
}
