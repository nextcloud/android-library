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

import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONObject;

/**
 * Create file with direct editing api
 */

public class DirectEditingCreateFileRemoteOperation extends RemoteOperation<String> {
    private static final String TAG = DirectEditingCreateFileRemoteOperation.class.getSimpleName();
    private static final String DIRECT_ENDPOINT = "/ocs/v2.php/apps/files/api/v1/directEditing/create";

    private final String path;
    private final String editor;
    private final String creator;
    private final String template;

    public DirectEditingCreateFileRemoteOperation(String path,
                                                  String editor,
                                                  String creator,
                                                  String template) {
        this.path = path;
        this.editor = editor;
        this.creator = creator;
        this.template = template;
    }

    public DirectEditingCreateFileRemoteOperation(String path, String editor, String creator) {
        this(path, editor, creator, "");
    }

    public RemoteOperationResult<String> run(NextcloudClient client) {
        RemoteOperationResult<String> result;
        PostMethod post = null;

        try {
            // request body
            JSONRequestBody jsonRequestBody = new JSONRequestBody("path", path);
            jsonRequestBody.put("editorId", editor);
            jsonRequestBody.put("creatorId", creator);

            if (!template.isEmpty()) {
                jsonRequestBody.put("templateId", template);
            }

            // post request
            post = new PostMethod(client.getBaseUri() + DIRECT_ENDPOINT + JSON_FORMAT, true, jsonRequestBody.get());

            int status = client.execute(post);

            if (status == HttpStatus.SC_OK) {
                String response = post.getResponseBodyAsString();

                // Parse the response
                JSONObject respJSON = new JSONObject(response);
                String url = (String) respJSON.getJSONObject("ocs").getJSONObject("data").get("url");

                result = new RemoteOperationResult<>(true, post);
                result.setResultData(url);
            } else {
                result = new RemoteOperationResult<>(false, post);
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Get all direct editing information failed: " + result.getLogMessage(),
                    result.getException());
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }
        return result;
    }
}
