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
