/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2018 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-FileCopyrightText: 2015 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-FileCopyrightText: 2015 masensio <masensio@solidgear.es>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.shares;

import android.net.Uri;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.GetMethod;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by masensio on 08/10/2015.
 *
 * Retrieves a list of sharees (possible targets of a share) from the ownCloud server.
 *
 * Currently only handles users and groups. Users in other OC servers (federation) should be added later.
 *
 * Depends on SHAREE API. {@see https://github.com/owncloud/documentation/issues/1626}
 *
 * Syntax:
 *    Entry point: ocs/v2.php/apps/files_sharing/api/v1/sharees
 *    HTTP method: GET
 *    url argument: itemType - string, required
 *    url argument: format - string, optional
 *    url argument: search - string, optional
 *    url arguments: perPage - int, optional
 *    url arguments: page - int, optional
 *
 * Status codes:
 *    100 - successful
 */
public class GetShareesRemoteOperation extends RemoteOperation<ArrayList<JSONObject>> {

    private static final String TAG = GetShareesRemoteOperation.class.getSimpleName();

    // OCS Routes
    private static final String OCS_ROUTE = "ocs/v2.php/apps/files_sharing/api/v1/sharees";    // from OC 8.2

    // Arguments - names
    private static final String PARAM_FORMAT = "format";
    private static final String PARAM_ITEM_TYPE = "itemType";
    private static final String PARAM_SEARCH = "search";
    private static final String PARAM_PAGE = "page";                //  default = 1
    private static final String PARAM_PER_PAGE = "perPage";         //  default = 200
    private static final String PARAM_LOOKUP = "lookup";

    // Arguments - constant values
    private static final String VALUE_FORMAT = "json";
    private static final String VALUE_ITEM_TYPE = "file";         //  to get the server search for users / groups
    private static final String VALUE_FALSE = "false";


    // JSON Node names
    private static final String NODE_OCS = "ocs";
    private static final String NODE_DATA = "data";
    private static final String NODE_EXACT = "exact";
    private static final String NODE_USERS = "users";
    private static final String NODE_GROUPS = "groups";
    private static final String NODE_REMOTES = "remotes";
    private static final String NODE_EMAILS = "emails";
    private static final String NODE_ROOMS = "rooms";
    private static final String NODE_CIRCLES = "circles";
    public static final String NODE_VALUE = "value";
    public static final String PROPERTY_LABEL = "label";
    public static final String PROPERTY_SHARE_TYPE = "shareType";
    public static final String PROPERTY_SHARE_WITH = "shareWith";
    public static final String PROPERTY_STATUS = "status";
    public static final String PROPERTY_MESSAGE = "message";
    public static final String PROPERTY_ICON = "icon";
    public static final String PROPERTY_CLEAR_AT = "clearAt";

    private final String searchString;
    private final int page;
    private final int perPage;

    /**
     * Constructor
     *
     * @param searchString string for searching users, optional
     * @param page         page index in the list of results; beginning in 1
     * @param perPage      maximum number of results in a single page
     */
    public GetShareesRemoteOperation(String searchString, int page, int perPage) {
        this.searchString = searchString;
        this.page = page;
        this.perPage = perPage;
    }

    @Override
    protected RemoteOperationResult<ArrayList<JSONObject>> run(OwnCloudClient client) {
        RemoteOperationResult<ArrayList<JSONObject>> result;
        int status;
        org.apache.commons.httpclient.methods.GetMethod get = null;

        try {
            Uri requestUri = client.getBaseUri();
            Uri.Builder uriBuilder = requestUri.buildUpon();
            uriBuilder.appendEncodedPath(OCS_ROUTE);
            uriBuilder.appendQueryParameter(PARAM_FORMAT, VALUE_FORMAT);
            uriBuilder.appendQueryParameter(PARAM_ITEM_TYPE, VALUE_ITEM_TYPE);
            uriBuilder.appendQueryParameter(PARAM_SEARCH, searchString);
            uriBuilder.appendQueryParameter(PARAM_PAGE, String.valueOf(page));
            uriBuilder.appendQueryParameter(PARAM_PER_PAGE, String.valueOf(perPage));
            uriBuilder.appendQueryParameter(PARAM_LOOKUP, VALUE_FALSE);

            // Get Method
            get = new org.apache.commons.httpclient.methods.GetMethod(uriBuilder.build().toString());
            get.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);

            status = client.executeMethod(get);

            if (isSuccess(status)) {
                String response = get.getResponseBodyAsString();
                Log_OC.d(TAG, "Successful response: " + response);

                // Parse the response
                JSONObject respJSON = new JSONObject(response);
                JSONObject respOCS = respJSON.getJSONObject(NODE_OCS);
                JSONObject respData = respOCS.getJSONObject(NODE_DATA);
                JSONObject respExact = respData.getJSONObject(NODE_EXACT);

                JSONArray respExactUsers = respExact.getJSONArray(NODE_USERS);
                JSONArray respExactGroups = respExact.getJSONArray(NODE_GROUPS);
                JSONArray respExactRemotes = respExact.getJSONArray(NODE_REMOTES);
                JSONArray respExactCircles;
                if (respExact.has(NODE_CIRCLES)) {
                    respExactCircles = respExact.getJSONArray(NODE_CIRCLES);
                } else {
                    respExactCircles = new JSONArray();
                }

                JSONArray respExactRooms;
                if (respExact.has(NODE_ROOMS)) {
                    respExactRooms = respExact.getJSONArray(NODE_ROOMS);
                } else {
                    respExactRooms = new JSONArray();
                }

                JSONArray respExactEmails = respExact.getJSONArray(NODE_EMAILS);
                JSONArray respPartialUsers = respData.getJSONArray(NODE_USERS);
                JSONArray respPartialGroups = respData.getJSONArray(NODE_GROUPS);
                JSONArray respPartialRemotes = respData.getJSONArray(NODE_REMOTES);
                JSONArray respPartialCircles;

                if (respData.has(NODE_CIRCLES)) {
                    respPartialCircles = respData.getJSONArray(NODE_CIRCLES);
                } else {
                    respPartialCircles = new JSONArray();
                }

                JSONArray respPartialRooms;

                if (respData.has(NODE_ROOMS)) {
                    respPartialRooms = respData.getJSONArray(NODE_ROOMS);
                } else {
                    respPartialRooms = new JSONArray();
                }

                JSONArray[] jsonResults = {
                    respExactUsers,
                    respExactGroups,
                    respExactRemotes,
                    respExactRooms,
                    respExactEmails,
                    respExactCircles,
                    respPartialUsers,
                    respPartialGroups,
                    respPartialRemotes,
                    respPartialRooms,
                    respPartialCircles
                };

                ArrayList<JSONObject> data = new ArrayList<>();
                for (JSONArray jsonResult : jsonResults) {
                    for (int j = 0; j < jsonResult.length(); j++) {
                        JSONObject jsonObject = jsonResult.getJSONObject(j);
                        data.add(jsonObject);
                        Log_OC.d(TAG, "*** Added item: " + jsonObject.getString(PROPERTY_LABEL));
                    }
                }

                // Result
                result = new RemoteOperationResult<>(true, get);
                result.setResultData(data);

                Log_OC.d(TAG, "*** Get Users or groups completed");

            } else {
                result = new RemoteOperationResult<>(false, get);
                String response = get.getResponseBodyAsString();
                Log_OC.e(TAG, "Failed response while getting users/groups from the server");

                if (response != null) {
                    Log_OC.e(TAG, "*** status code: " + status + "; response message: " + response);
                } else {
                    Log_OC.e(TAG, "*** status code: " + status);
                }
            }

        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Exception while getting users/groups", e);

        } finally {
            if (get != null) {
                get.releaseConnection();
            }
        }
        return result;
    }

    @Override
    public RemoteOperationResult<ArrayList<JSONObject>> run(NextcloudClient client) {
        RemoteOperationResult<ArrayList<JSONObject>> result;
        int status;
        GetMethod get = null;

        try {
            Uri requestUri = client.getBaseUri();
            Uri.Builder uriBuilder = requestUri.buildUpon();
            uriBuilder.appendEncodedPath(OCS_ROUTE);
            uriBuilder.appendQueryParameter(PARAM_FORMAT, VALUE_FORMAT);
            uriBuilder.appendQueryParameter(PARAM_ITEM_TYPE, VALUE_ITEM_TYPE);
            uriBuilder.appendQueryParameter(PARAM_SEARCH, searchString);
            uriBuilder.appendQueryParameter(PARAM_PAGE, String.valueOf(page));
            uriBuilder.appendQueryParameter(PARAM_PER_PAGE, String.valueOf(perPage));
            uriBuilder.appendQueryParameter(PARAM_LOOKUP, VALUE_FALSE);

            // Get Method
            get = new GetMethod(uriBuilder.build().toString(), true);

            status = client.execute(get);

            if (isSuccess(status)) {
                String response = get.getResponseBodyAsString();
                Log_OC.d(TAG, "Successful response: " + response);

                // Parse the response
                JSONObject respJSON = new JSONObject(response);
                JSONObject respOCS = respJSON.getJSONObject(NODE_OCS);
                JSONObject respData = respOCS.getJSONObject(NODE_DATA);
                JSONObject respExact = respData.getJSONObject(NODE_EXACT);

                JSONArray respExactUsers = respExact.getJSONArray(NODE_USERS);
                JSONArray respExactGroups = respExact.getJSONArray(NODE_GROUPS);
                JSONArray respExactRemotes = respExact.getJSONArray(NODE_REMOTES);
                JSONArray respExactCircles;
                if (respExact.has(NODE_CIRCLES)) {
                    respExactCircles = respExact.getJSONArray(NODE_CIRCLES);
                } else {
                    respExactCircles = new JSONArray();
                }

                JSONArray respExactRooms;
                if (respExact.has(NODE_ROOMS)) {
                    respExactRooms = respExact.getJSONArray(NODE_ROOMS);
                } else {
                    respExactRooms = new JSONArray();
                }

                JSONArray respExactEmails = respExact.getJSONArray(NODE_EMAILS);
                JSONArray respPartialUsers = respData.getJSONArray(NODE_USERS);
                JSONArray respPartialGroups = respData.getJSONArray(NODE_GROUPS);
                JSONArray respPartialRemotes = respData.getJSONArray(NODE_REMOTES);
                JSONArray respPartialCircles;

                if (respData.has(NODE_CIRCLES)) {
                    respPartialCircles = respData.getJSONArray(NODE_CIRCLES);
                } else {
                    respPartialCircles = new JSONArray();
                }

                JSONArray respPartialRooms;
                
                if (respData.has(NODE_ROOMS)) {
                    respPartialRooms = respData.getJSONArray(NODE_ROOMS);
                } else {
                    respPartialRooms = new JSONArray();
                }
                
                JSONArray[] jsonResults = {
                        respExactUsers,
                        respExactGroups,
                        respExactRemotes,
                        respExactRooms,
                        respExactEmails,
                        respExactCircles,
                        respPartialUsers,
                        respPartialGroups,
                        respPartialRemotes,
                        respPartialRooms,
                        respPartialCircles
                };

                ArrayList<JSONObject> data = new ArrayList<>();
                for (JSONArray jsonResult : jsonResults) {
                    for (int j = 0; j < jsonResult.length(); j++) {
                        JSONObject jsonObject = jsonResult.getJSONObject(j);
                        data.add(jsonObject);
                        Log_OC.d(TAG, "*** Added item: " + jsonObject.getString(PROPERTY_LABEL));
                    }
                }

                // Result
                result = new RemoteOperationResult<>(true, get);
                result.setResultData(data);

                Log_OC.d(TAG, "*** Get Users or groups completed");

            } else {
                result = new RemoteOperationResult<>(false, get);
                String response = get.getResponseBodyAsString();
                Log_OC.e(TAG, "Failed response while getting users/groups from the server");
                Log_OC.e(TAG, "*** status code: " + status + "; response message: " + response);
            }

        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Exception while getting users/groups", e);

        } finally {
            if (get != null) {
                get.releaseConnection();
            }
        }
        return result;
    }

    private boolean isSuccess(int status) {
        return (status == HttpStatus.SC_OK);
    }
}
