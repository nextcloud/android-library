/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.richWorkspace;

import com.google.gson.GsonBuilder;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.Utf8PostMethod;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Get direct editing url for rich workspace
 */

public class RichWorkspaceDirectEditingRemoteOperation extends RemoteOperation {
    private static final String TAG = RichWorkspaceDirectEditingRemoteOperation.class.getSimpleName();
    private static final int SYNC_READ_TIMEOUT = 40000;
    private static final int SYNC_CONNECTION_TIMEOUT = 5000;
    private static final String DIRECT_ENDPOINT = "/ocs/v2.php/apps/text/workspace/direct";
    private static final String PATH = "path";

    private String path;

    public RichWorkspaceDirectEditingRemoteOperation(String path) {
        this.path = path;
    }

    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result;
        Utf8PostMethod postMethod = null;

        try {
            postMethod = new Utf8PostMethod(client.getBaseUri() + DIRECT_ENDPOINT + JSON_FORMAT);
            postMethod.addRequestHeader(CONTENT_TYPE, JSON_ENCODED);
            postMethod.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);

            Map<String, String> values = new HashMap<>();
            values.put(PATH, path);

            String json = new GsonBuilder().create().toJson(values, Map.class);

            postMethod.setRequestEntity(new StringRequestEntity(json));

            int status = client.executeMethod(postMethod, SYNC_READ_TIMEOUT, SYNC_CONNECTION_TIMEOUT);

            if (status == HttpStatus.SC_OK) {
                String response = postMethod.getResponseBodyAsString();

                // Parse the response
                JSONObject respJSON = new JSONObject(response);
                String url = (String) respJSON.getJSONObject("ocs").getJSONObject("data").get("url");

                result = new RemoteOperationResult(true, postMethod);
                result.setSingleData(url);
            } else {
                result = new RemoteOperationResult(false, postMethod);
                client.exhaustResponse(postMethod.getResponseBodyAsStream());
            }
        } catch (Exception e) {
            result = new RemoteOperationResult(e);
            Log_OC.e(TAG, "Get edit url for rich workspace failed: " + result.getLogMessage(),
                    result.getException());
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
        return result;
    }
}
