/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 TSI-mc <surinder.kumar@t-systems.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package com.owncloud.android.lib.resources.albums;

import static org.apache.commons.httpclient.HttpStatus.SC_MULTI_STATUS;
import static org.apache.commons.httpclient.HttpStatus.SC_OK;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.nextcloud.common.SessionTimeOut;
import com.nextcloud.common.SessionTimeOutKt;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.network.WebdavUtils;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;

import java.util.ArrayList;
import java.util.List;

public class ReadAlbumsRemoteOperation extends RemoteOperation<List<PhotoAlbumEntry>> {

    private static final String TAG = ReadAlbumsRemoteOperation.class.getSimpleName();
    private final String mAlbumRemotePath;
    private final SessionTimeOut sessionTimeOut;


    public ReadAlbumsRemoteOperation() {
        this(null);
    }

    public ReadAlbumsRemoteOperation(@Nullable String mAlbumRemotePath) {
        this(mAlbumRemotePath, SessionTimeOutKt.getDefaultSessionTimeOut());
    }

    public ReadAlbumsRemoteOperation(@Nullable String mAlbumRemotePath, SessionTimeOut sessionTimeOut) {
        this.mAlbumRemotePath = mAlbumRemotePath;
        this.sessionTimeOut = sessionTimeOut;
    }

    /**
     * Performs the operation.
     *
     * @param client Client object to communicate with the remote ownCloud server.
     */
    @Override
    protected RemoteOperationResult<List<PhotoAlbumEntry>> run(OwnCloudClient client) {
        PropFindMethod propfind = null;
        RemoteOperationResult<List<PhotoAlbumEntry>> result;
        String url = client.getBaseUri() + "/remote.php/dav/photos/" + client.getUserId() + "/albums";
        if (!TextUtils.isEmpty(mAlbumRemotePath)) {
            url += WebdavUtils.encodePath(mAlbumRemotePath);
        }
        try {
            propfind = new PropFindMethod(url, WebdavUtils.getAlbumPropSet(), DavConstants.DEPTH_1);
            int status = client.executeMethod(propfind, sessionTimeOut.getReadTimeOut(), sessionTimeOut.getConnectionTimeOut());
            boolean isSuccess = status == SC_MULTI_STATUS || status == SC_OK;
            if (isSuccess) {
                MultiStatus multiStatus = propfind.getResponseBodyAsMultiStatus();
                List<PhotoAlbumEntry> albumsList = new ArrayList<>();
                for (MultiStatusResponse response : multiStatus.getResponses()) {
                    int st = response.getStatus()[0].getStatusCode();
                    if (st == SC_OK) {
                        PhotoAlbumEntry entry = new PhotoAlbumEntry(response);
                        albumsList.add(entry);
                    }
                }
                result = new RemoteOperationResult(true, propfind);
                result.setResultData(albumsList);
            } else {
                result = new RemoteOperationResult(false, propfind);
                client.exhaustResponse(propfind.getResponseBodyAsStream());
            }
        } catch (Exception var13) {
            Exception e = var13;
            result = new RemoteOperationResult(e);
            Log_OC.e(TAG, "Read album " + " failed: " + result.getLogMessage(), result.getException());
        } finally {
            if (propfind != null) {
                propfind.releaseConnection();
            }

        }

        return result;
    }
}
