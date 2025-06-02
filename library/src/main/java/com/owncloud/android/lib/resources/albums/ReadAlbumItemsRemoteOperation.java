/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 TSI-mc <surinder.kumar@t-systems.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.resources.albums;

import com.nextcloud.common.SessionTimeOut;
import com.nextcloud.common.SessionTimeOutKt;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.network.WebdavUtils;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.common.utils.WebDavFileUtils;
import com.owncloud.android.lib.resources.files.model.RemoteFile;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;

import java.util.ArrayList;
import java.util.List;

public class ReadAlbumItemsRemoteOperation extends RemoteOperation<List<RemoteFile>> {

    private static final String TAG = ReadAlbumItemsRemoteOperation.class.getSimpleName();
    private final String mRemotePath;
    private final SessionTimeOut sessionTimeOut;

    public ReadAlbumItemsRemoteOperation(String mRemotePath) {
        this(mRemotePath, SessionTimeOutKt.getDefaultSessionTimeOut());
    }

    public ReadAlbumItemsRemoteOperation(String mRemotePath, SessionTimeOut sessionTimeOut) {
        this.mRemotePath = mRemotePath;
        this.sessionTimeOut = sessionTimeOut;
    }

    protected RemoteOperationResult<List<RemoteFile>> run(OwnCloudClient client) {
        RemoteOperationResult<List<RemoteFile>> result = null;
        PropFindMethod query = null;
        String url = client.getBaseUri() + "/remote.php/dav/photos/" + client.getUserId() + "/albums" + WebdavUtils.encodePath(mRemotePath);
        try {
            // remote request
            query = new PropFindMethod(url,
                WebdavUtils.getAllPropSet(), // PropFind Properties
                DavConstants.DEPTH_1);
            int status = client.executeMethod(query, sessionTimeOut.getReadTimeOut(), sessionTimeOut.getConnectionTimeOut());

            // check and process response
            boolean isSuccess = (status == HttpStatus.SC_MULTI_STATUS || status == HttpStatus.SC_OK);

            if (isSuccess) {
                // get data from remote folder
                MultiStatus dataInServer = query.getResponseBodyAsMultiStatus();
                ArrayList<RemoteFile> mFolderAndFiles = new WebDavFileUtils().readAlbumData(dataInServer, client);

                // Result of the operation
                result = new RemoteOperationResult<>(true, query);
                // Add data to the result
                if (result.isSuccess()) {
                    result.setResultData(mFolderAndFiles);
                }
            } else {
                // synchronization failed
                client.exhaustResponse(query.getResponseBodyAsStream());
                result = new RemoteOperationResult<>(false, query);
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
        } finally {
            if (query != null) {
                query.releaseConnection(); // let the connection available for other methods
            }

            if (result == null) {
                result = new RemoteOperationResult(new Exception("unknown error"));
                Log_OC.e(TAG, "Synchronized " + mRemotePath + ": failed");
            } else {
                if (result.isSuccess()) {
                    Log_OC.i(TAG, "Synchronized " + mRemotePath + ": " + result.getLogMessage());
                } else {
                    if (result.isException()) {
                        Log_OC.e(TAG, "Synchronized " + mRemotePath + ": " + result.getLogMessage(),
                            result.getException());
                    } else {
                        Log_OC.e(TAG, "Synchronized " + mRemotePath + ": " + result.getLogMessage());
                    }
                }
            }
        }

        return result;
    }
}
