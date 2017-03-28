package com.owncloud.android.lib.resources.activities;

import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.activities.models.Activity;
import com.owncloud.android.lib.resources.shares.GetRemoteShareesOperation;
import com.owncloud.android.lib.resources.shares.ShareToRemoteOperationResultParser;
import com.owncloud.android.lib.resources.shares.ShareXMLParser;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alejandro on 27/03/17.
 */

public class GetRemoteActivitiesOperation extends RemoteOperation{


    private static final String TAG = GetRemoteActivitiesOperation.class.getSimpleName();

    // OCS Routes
    private static final String OCS_ROUTE = "/apps/activity/api/v2/activity";

    // JSON Node names
    private static final String NODE_OCS = "ocs";

    private static final String NODE_DATA = "data";

    /**
     * Date pattern according to h
     * ttp://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
     * for "2004-02-12T15:19:21+00:00"
     */
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

    /**
     * This status code means that there is no app that can generate notifications.
     * Slow down the polling to once per hour.
     */
    public static final String STATUS_NO_CONTENT = "204";

    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result = null;
        int status = -1;
        GetMethod get = null;
        ArrayList<Object> activities;
        String url = client.getBaseUri() + OCS_ROUTE;
        Log_OC.d(TAG, "URL: " + url);

        try {

            get = new GetMethod(url);
            get.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);

            status = client.executeMethod(get);
            String response = get.getResponseBodyAsString();

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

    private ArrayList<Object> parseResult(String response) throws JSONException {
        JsonParser jsonParser = new JsonParser();
        JsonObject jo = (JsonObject)jsonParser.parse(response);
        JsonArray jsonDataArray = jo.getAsJsonObject(NODE_OCS).getAsJsonArray(NODE_DATA);

        Gson gson = new Gson();
        Type listType = new TypeToken<List<Activity>>(){}.getType();
        ArrayList<Object> activities = gson.fromJson(jsonDataArray, listType);

        return activities;
    }

    private boolean isSuccess(int status) {
        return (status == HttpStatus.SC_OK);
    }

}
