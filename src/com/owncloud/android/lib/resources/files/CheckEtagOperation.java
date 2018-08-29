/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2018 Tobias Kaminsky
 * Copyright (C) 2018 Nextcloud GmbH.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.owncloud.android.lib.resources.files;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.network.WebdavUtils;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;

import java.util.ArrayList;

/**
 * Check if file is up to date, by checking only eTag
 */
public class CheckEtagOperation extends RemoteOperation {

    private static final int SYNC_READ_TIMEOUT = 40000;
    private static final int SYNC_CONNECTION_TIMEOUT = 5000;
    private static final String TAG = CheckEtagOperation.class.getSimpleName();

    private String path;
    private String expectedEtag;

    public CheckEtagOperation(String path, String expectedEtag) {
        this.path = path;
        this.expectedEtag = expectedEtag;
    }


    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        try {
            DavPropertyNameSet propSet = new DavPropertyNameSet();
            propSet.add(DavPropertyName.GETETAG);

            PropFindMethod propfind = new PropFindMethod(client.getWebdavUri() + WebdavUtils.encodePath(path),
                    propSet, 0);
            int status = client.executeMethod(propfind, SYNC_READ_TIMEOUT, SYNC_CONNECTION_TIMEOUT);

            if (status == HttpStatus.SC_MULTI_STATUS || status == HttpStatus.SC_OK) {
                MultiStatusResponse resp = propfind.getResponseBodyAsMultiStatus().getResponses()[0];

                String etag = WebdavUtils.parseEtag((String) resp.getProperties(HttpStatus.SC_OK)
                        .get(DavPropertyName.GETETAG).getValue());

                if (etag.equals(expectedEtag)) {
                    return new RemoteOperationResult(RemoteOperationResult.ResultCode.ETAG_UNCHANGED);
                } else {
                    RemoteOperationResult result = new RemoteOperationResult(
                            RemoteOperationResult.ResultCode.ETAG_CHANGED);

                    ArrayList<Object> list = new ArrayList<>();
                    list.add(etag);
                    result.setData(list);

                    return result;
                }
            }
            
            if (status == HttpStatus.SC_NOT_FOUND) {
                return new RemoteOperationResult(RemoteOperationResult.ResultCode.FILE_NOT_FOUND);
            }
        } catch (Exception e) {
            Log_OC.e(TAG, "Error while retrieving eTag");
        }

        return new RemoteOperationResult(RemoteOperationResult.ResultCode.ETAG_CHANGED);
    }
}
