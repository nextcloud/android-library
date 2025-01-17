/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2018 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files;

import com.nextcloud.common.SessionTimeOut;
import com.nextcloud.common.SessionTimeOutKt;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.network.WebdavUtils;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Check if file is up to date, by checking only eTag
 */
public class CheckEtagRemoteOperation extends RemoteOperation {

    private static final String TAG = CheckEtagRemoteOperation.class.getSimpleName();

    private final String path;
    private final String expectedEtag;

    private final SessionTimeOut sessionTimeOut;

    public CheckEtagRemoteOperation(String path, String expectedEtag) {
        this(path, expectedEtag, SessionTimeOutKt.getDefaultSessionTimeOut());
    }

    public CheckEtagRemoteOperation(String path, String expectedEtag, SessionTimeOut sessionTimeOut) {
        this.path = path;
        this.expectedEtag = expectedEtag;
        this.sessionTimeOut = sessionTimeOut;
    }

    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        PropFindMethod propfind = null;
        
        try {
            DavPropertyNameSet propSet = new DavPropertyNameSet();
            propSet.add(DavPropertyName.GETETAG);

            propfind = new PropFindMethod(client.getFilesDavUri(path),
                    propSet,
                    0);
            int status = client.executeMethod(propfind, sessionTimeOut.getReadTimeOut(), sessionTimeOut.getConnectionTimeOut());

            if (status == HttpStatus.SC_MULTI_STATUS || status == HttpStatus.SC_OK) {
                MultiStatusResponse resp = propfind.getResponseBodyAsMultiStatus().getResponses()[0];

                String etag = WebdavUtils.parseEtag((String) resp.getProperties(HttpStatus.SC_OK)
                        .get(DavPropertyName.GETETAG).getValue());

                if (etag.equals(expectedEtag)) {
                    return new RemoteOperationResult<>(ResultCode.ETAG_UNCHANGED);
                } else {
                    final var result = new RemoteOperationResult<>(ResultCode.ETAG_CHANGED);

                    ArrayList<Object> list = new ArrayList<>();
                    list.add(etag);
                    result.setData(list);

                    return result;
                }
            }

            if (status == HttpStatus.SC_NOT_FOUND) {
                return new RemoteOperationResult<>(ResultCode.FILE_NOT_FOUND);
            }
        } catch (DavException | IOException e) {
            Log_OC.e(TAG, "Error while retrieving eTag");
        } finally {
            if (propfind != null) {
                propfind.releaseConnection();
            }
        }

        return new RemoteOperationResult<>(ResultCode.ETAG_CHANGED);
    }
}
