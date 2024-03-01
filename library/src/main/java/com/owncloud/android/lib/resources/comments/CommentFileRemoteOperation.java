/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2018 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
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
    protected RemoteOperationResult run(OwnCloudClient client) {

        Utf8PostMethod postMethod = null;
        RemoteOperationResult result;
        try {
            String url = client.getCommentsUri(fileId);
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
