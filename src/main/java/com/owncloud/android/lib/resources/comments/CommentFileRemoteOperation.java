/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2018 Tobias Kaminsky
 *   Copyright (C) 2018 Nextcloud GmbH
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

package com.owncloud.android.lib.resources.comments;

import android.util.Log;

import com.google.gson.GsonBuilder;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.Utf8PostMethod;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Comment file
 */
public class CommentFileRemoteOperation extends RemoteOperation {

    private static final String TAG = CommentFileRemoteOperation.class.getSimpleName();
    private static final int POST_READ_TIMEOUT = 30000;
    private static final int POST_CONNECTION_TIMEOUT = 5000;

    private static final String ACTOR_ID = "actorId";
    private static final String ACTOR_TYPE = "actorType";
    private static final String ACTOR_TYPE_VALUE = "users";
    private static final String VERB = "verb";
    private static final String VERB_VALUE = "comment";
    private static final String MESSAGE = "message";

    private String message;
    private String fileId;

    /**
     * Constructor
     *
     * @param message Comment to store
     */
    public CommentFileRemoteOperation(String message, String fileId) {
        this.message = message;
        this.fileId = fileId;
    }

    /**
     * Performs the operation.
     *
     * @param client Client object to communicate with the remote ownCloud server.
     */
    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {

        Utf8PostMethod postMethod = null;
        RemoteOperationResult result;
        try {
            String url = client.getNewWebdavUri() + "/comments/files/" + fileId;
            postMethod = new Utf8PostMethod(url);
            postMethod.addRequestHeader("Content-type", "application/json");

            Map<String, String> values = new HashMap<>();
            values.put(ACTOR_ID, client.getUserId());
            values.put(ACTOR_TYPE, ACTOR_TYPE_VALUE);
            values.put(VERB, VERB_VALUE);
            values.put(MESSAGE, message);

            String json = new GsonBuilder().create().toJson(values, Map.class);

            postMethod.setRequestEntity(new StringRequestEntity(json));

            int status = client.executeMethod(postMethod, POST_READ_TIMEOUT, POST_CONNECTION_TIMEOUT);

            result = new RemoteOperationResult(isSuccess(status), postMethod);

            client.exhaustResponse(postMethod.getResponseBodyAsStream());
        } catch (IOException e) {
            result = new RemoteOperationResult(e);
            Log.e(TAG, "Post comment to file with id " + fileId + " failed: " + result.getLogMessage(), e);
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }

        return result;
    }

    private boolean isSuccess(int status) {
        return status == HttpStatus.SC_CREATED;
    }
}
