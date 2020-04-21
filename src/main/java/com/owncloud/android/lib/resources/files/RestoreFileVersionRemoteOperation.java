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

package com.owncloud.android.lib.resources.files;

import android.net.Uri;
import android.util.Log;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.model.FileVersion;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.jackrabbit.webdav.client.methods.MoveMethod;

import java.io.IOException;


/**
 * Restore a {@link FileVersion}.
 */
public class RestoreFileVersionRemoteOperation extends RemoteOperation {

    private static final String TAG = RestoreFileVersionRemoteOperation.class.getSimpleName();
    private static final int RESTORE_READ_TIMEOUT = 30000;
    private static final int RESTORE_CONNECTION_TIMEOUT = 5000;

    private String fileId;
    private String fileName;

    /**
     * Constructor
     *
     * @param fileId   fileId
     * @param fileName version date in unixtime
     */
    public RestoreFileVersionRemoteOperation(String fileId, String fileName) {
        this.fileId = fileId;
        this.fileName = fileName;
    }

    /**
     * Performs the operation.
     *
     * @param client Client object to communicate with the remote ownCloud server.
     */
    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {

        MoveMethod move = null;
        RemoteOperationResult result;
        try {
            String source = client.getNewWebdavUri() + "/versions/" + client.getUserId() + "/versions/" + fileId + "/"
                    + Uri.encode(fileName);
            String target = client.getNewWebdavUri() + "/versions/" + client.getUserId() + "/restore/" + fileId;

            move = new MoveMethod(source, target, true);
            int status = client.executeMethod(move, RESTORE_READ_TIMEOUT, RESTORE_CONNECTION_TIMEOUT);

            result = new RemoteOperationResult(isSuccess(status), move);

            client.exhaustResponse(move.getResponseBodyAsStream());
        } catch (IOException e) {
            result = new RemoteOperationResult(e);
            Log.e(TAG, "Restore file version with id " + fileId + " failed: " + result.getLogMessage(), e);
        } finally {
            if (move != null) {
                move.releaseConnection();
            }
        }

        return result;
    }

    private boolean isSuccess(int status) {
        return status == HttpStatus.SC_CREATED || status == HttpStatus.SC_NO_CONTENT;
    }
}
