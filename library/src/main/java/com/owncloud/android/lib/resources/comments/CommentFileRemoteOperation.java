/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2018 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
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
            JSONRequestBody jsonRequestBody = new JSONRequestBody();
            jsonRequestBody.put(ACTOR_ID, client.getUserId());
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
