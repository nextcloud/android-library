/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019-2024 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.directediting;

import com.nextcloud.common.JSONRequestBody;
import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.PostMethod;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.json.JSONObject;

/**
 * open file for direct editing
 */

public class DirectEditingOpenFileRemoteOperation extends RemoteOperation<String> {
    private static final String TAG = DirectEditingOpenFileRemoteOperation.class.getSimpleName();
    private static final String DIRECT_ENDPOINT = "/ocs/v2.php/apps/files/api/v1/directEditing/open";

    private final String filePath;
    private final String editor;

    public DirectEditingOpenFileRemoteOperation(String filePath, String editor) {
        this.filePath = filePath;
        this.editor = editor;
    }

    public RemoteOperationResult<String> run(NextcloudClient client) {
        RemoteOperationResult<String> result;
        PostMethod postMethod = null;

        try {
            // request body
            JSONRequestBody jsonRequestBody = new JSONRequestBody("path", filePath);
            jsonRequestBody.put("editorId", editor);

            // post request
            postMethod = new PostMethod(client.getBaseUri() + DIRECT_ENDPOINT + JSON_FORMAT, true,
                                        jsonRequestBody.get());

            client.execute(postMethod);

            if (postMethod.isSuccess()) {
                String response = postMethod.getResponseBodyAsString();

                // Parse the response
                JSONObject respJSON = new JSONObject(response);
                String url = (String) respJSON.getJSONObject("ocs").getJSONObject("data").get("url");

                result = new RemoteOperationResult<>(true, postMethod);
                result.setResultData(url);
            } else {
                result = new RemoteOperationResult<>(false, postMethod);
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Get all direct editing information failed: " + result.getLogMessage(),
                     result.getException());
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
        return result;
    }
}
