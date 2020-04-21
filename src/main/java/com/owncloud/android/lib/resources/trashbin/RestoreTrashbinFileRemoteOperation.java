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

package com.owncloud.android.lib.resources.trashbin;

import android.net.Uri;
import android.util.Log;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.network.WebdavUtils;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.trashbin.model.TrashbinFile;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.jackrabbit.webdav.client.methods.MoveMethod;

import java.io.IOException;


/**
 * Restore a {@link TrashbinFile}.
 */
public class RestoreTrashbinFileRemoteOperation extends RemoteOperation {

    private static final String TAG = RestoreTrashbinFileRemoteOperation.class.getSimpleName();
    private static final int RESTORE_READ_TIMEOUT = 30000;
    private static final int RESTORE_CONNECTION_TIMEOUT = 5000;

    private String sourcePath;
    private String fileName;

    /**
     * Constructor
     *
     * @param sourcePath Remote path of the {@link TrashbinFile} to restore
     * @param fileName   original filename
     */
    public RestoreTrashbinFileRemoteOperation(String sourcePath, String fileName) {
        this.sourcePath = sourcePath;
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
            String source = client.getNewWebdavUri() + WebdavUtils.encodePath(sourcePath);
            String target = client.getNewWebdavUri() + "/trashbin/" + client.getUserId() + "/restore/" +
                    Uri.encode(fileName);

            move = new MoveMethod(source, target, true);
            int status = client.executeMethod(move, RESTORE_READ_TIMEOUT, RESTORE_CONNECTION_TIMEOUT);

            result = new RemoteOperationResult(isSuccess(status), move);

            client.exhaustResponse(move.getResponseBodyAsStream());
        } catch (IOException e) {
            result = new RemoteOperationResult(e);
            Log.e(TAG, "Restore trashbin file " + sourcePath + " failed: " + result.getLogMessage(), e);
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
