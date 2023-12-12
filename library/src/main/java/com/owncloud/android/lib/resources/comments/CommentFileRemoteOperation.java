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

import com.nextcloud.common.JSONRequestBody;
import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.PostMethod;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;

import org.apache.commons.httpclient.HttpStatus;

import java.io.IOException;

/**
 * Comment file
 */
public class CommentFileRemoteOperation extends RemoteOperation<Void> {

    private static final String TAG = CommentFileRemoteOperation.class.getSimpleName();
    private static final String ACTOR_ID = "actorId";
    private static final String ACTOR_TYPE = "actorType";
    private static final String ACTOR_TYPE_VALUE = "users";
    private static final String VERB = "verb";
    private static final String VERB_VALUE = "comment";
    private static final String MESSAGE = "message";

    private final String message;
    private final long fileId;

    /**
     * Constructor
     *
     * @param message Comment to store
     */
    public CommentFileRemoteOperation(String message, long fileId) {
        this.message = message;
        this.fileId = fileId;
    }

    /**
     * Performs the operation.
     *
     * @param client Client object to communicate with the remote ownCloud server.
     */
    @Override
    public RemoteOperationResult<Void> run(NextcloudClient client) {

        PostMethod postMethod = null;
        RemoteOperationResult<Void> result;
        try {
            // request body
            JSONRequestBody jsonRequestBody = new JSONRequestBody(ACTOR_ID, client.getUserId());
            jsonRequestBody.put(ACTOR_TYPE, ACTOR_TYPE_VALUE);
            jsonRequestBody.put(VERB, VERB_VALUE);
            jsonRequestBody.put(MESSAGE, message);

            // post method
            String url = client.getCommentsUri(fileId);
            postMethod = new PostMethod(url, false, jsonRequestBody.get());

            int status = client.execute(postMethod);

            result = new RemoteOperationResult<>(status == HttpStatus.SC_CREATED, postMethod);

        } catch (IOException e) {
            result = new RemoteOperationResult<>(e);
            Log.e(TAG, "Post comment to file with id " + fileId + " failed: " + result.getLogMessage(), e);
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }

        return result;
    }
}
