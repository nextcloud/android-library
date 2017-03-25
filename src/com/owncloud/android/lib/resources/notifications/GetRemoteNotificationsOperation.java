/*  Nextcloud Android Library is available under MIT license
 *   Copyright (C) 2017 Andy Scherzinger
 *   Copyright (C) 2017 Nextcloud GmbH
 *
 *   @author Andy Scherzinger
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

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides the remote notifications from the server handling the following data structure
 * accessible via the notifications endpoint at {@value OCS_ROUTE_LIST}, specified at
 * {@link "https://github.com/nextcloud/notifications/blob/master/docs/ocs-endpoint-v2.md"}.
 */
public class GetRemoteNotificationsOperation extends RemoteOperation {

    // OCS Route
    private static final String OCS_ROUTE_LIST =
            "/ocs/v2.php/apps/notifications/api/v2/notifications";

    private static final String TAG = GetRemoteNotificationsOperation.class.getSimpleName();

    // JSON Node names
    private static final String NODE_OCS = "ocs";

    private static final String NODE_OCS_META = "meta";
    private static final String NODE_OCS_META_STATUS = "status";
    private static final String NODE_OCS_META_STATUS_CODE = "statuscode";
    private static final String NODE_OCS_META_STATUS_MESSAGE = "message";

    private static final String NODE_DATA = "data";
    private static final String NODE_NOTIFICATION_ID = "notification_id";
    private static final String NODE_APP = "app";
    private static final String NODE_USER = "user";
    private static final String NODE_DATE_TIME = "datetime";
    private static final String NODE_OBJECT_TYPE = "object_type";
    private static final String NODE_OBJECT_ID = "object_id";
    private static final String NODE_SUBJECT = "subject";

    private static final String NODE_SUBJECT_RICH = "subjectRich";
    private static final String NODE_SUBJECT_RICH_PARAMS = "subjectRichParameters";
    private static final String NODE_SUBJECT_RICH_PARAMS_SHARE = "share";
    private static final String NODE_SUBJECT_RICH_PARAMS_SHARE_TYPE = "type";
    private static final String NODE_SUBJECT_RICH_PARAMS_SHARE_TYPE_ID = "id";
    private static final String NODE_SUBJECT_RICH_PARAMS_SHARE_TYPE_NAME = "name";

    private static final String NODE_SUBJECT_RICH_PARAMS_SHARE_USER = "user";
    private static final String NODE_SUBJECT_RICH_PARAMS_SHARE_USER_TYPE = "type";
    private static final String NODE_SUBJECT_RICH_PARAMS_SHARE_USER_TYPE_ID = "id";
    private static final String NODE_SUBJECT_RICH_PARAMS_SHARE_USER_TYPE_NAME = "name";
    private static final String NODE_SUBJECT_RICH_PARAMS_SHARE_USER_TYPE_SERVER = "server";

    private static final String NODE_MESSAGE = "message";
    private static final String NODE_MESSAGE_RICH = "messageRich";
    private static final String NODE_MESSAGE_RICH_PARAMS = "messageRichParameters";
    private static final String NODE_LINK = "link";
    private static final String NODE_ICON = "icon";
    private static final String NODE_ACTIONS = "actions";
    private static final String NODE_ACTIONS_LABEL = "label";
    private static final String NODE_ACTIONS_LABEL_LINK = "link";
    private static final String NODE_ACTIONS_LABEL_TYPE = "type";
    private static final String NODE_ACTIONS_LABEL_PRIMARY = "primary";

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
        String url = client.getBaseUri() + OCS_ROUTE_LIST;

        // get the notifications
        try {
            get = new GetMethod(url);
            get.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);

            status = client.executeMethod(get);

            if (isSuccess(status)) {
                String response = get.getResponseBodyAsString();
                Log_OC.d(TAG, "Successful response: " + response);

                // Parse the response
                Notification notification = parseResult(response);
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

    private Notification parseResult(String response) throws JSONException {
        JSONObject respJSON = new JSONObject(response);
        JSONObject respOCS = respJSON.getJSONObject(NODE_OCS);
        JSONObject respData = respOCS.getJSONObject(NODE_DATA);

        Notification notification = new Notification();

        notification.setNotificationId(respData.getInt(NODE_NOTIFICATION_ID));
        notification.setApp(respData.getString(NODE_APP));
        notification.setUser(respData.getString(NODE_USER));
        notification.setDatetime(respData.getString(NODE_DATE_TIME));
        notification.setObject_type(respData.getString(NODE_OBJECT_TYPE));
        notification.setObject_id(respData.getString(NODE_OBJECT_ID));
        notification.setSubject(respData.getString(NODE_SUBJECT));

        // optional parameters
        //v2+ only
        if (respData.has(NODE_SUBJECT_RICH)) {
            notification.setIcon(respData.getString(NODE_SUBJECT_RICH));
        }

        //v2+ only
        if (respData.has(NODE_SUBJECT_RICH_PARAMS)) {
            // Todo loop through array
        }

        if (respData.has(NODE_MESSAGE)) {
            notification.setMessage(respData.getString(NODE_MESSAGE));
        }

        //v2+ only
        if (respData.has(NODE_MESSAGE_RICH)) {
            notification.setMessage(respData.getString(NODE_MESSAGE_RICH));
        }

        //v2+ only
        if (respData.has(NODE_MESSAGE_RICH_PARAMS)) {
            // Todo loop through array
            List<RichObject> messageRichObjects = new ArrayList<RichObject>();
        }

        if (respData.has(NODE_LINK)) {
            notification.setLink(respData.getString(NODE_LINK));
        }

        //v2+ only
        if (respData.has(NODE_ICON)) {
            notification.setIcon(respData.getString(NODE_ICON));
        }

        if (respData.has(NODE_ACTIONS)) {
            // Todo loop through array
            List<Action> actions = new ArrayList<Action>();
        }

        return notification;
    }

    private boolean isSuccess(int status) {
        return (status == HttpStatus.SC_OK);
    }
}
