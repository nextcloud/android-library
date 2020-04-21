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
public class CheckEtagRemoteOperation extends RemoteOperation {

    private static final int SYNC_READ_TIMEOUT = 40000;
    private static final int SYNC_CONNECTION_TIMEOUT = 5000;
    private static final String TAG = CheckEtagRemoteOperation.class.getSimpleName();

    private String path;
    private String expectedEtag;

    public CheckEtagRemoteOperation(String path, String expectedEtag) {
        this.path = path;
        this.expectedEtag = expectedEtag;
    }


    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        PropFindMethod propfind = null;
        
        try {
            DavPropertyNameSet propSet = new DavPropertyNameSet();
            propSet.add(DavPropertyName.GETETAG);

            propfind = new PropFindMethod(client.getWebdavUri() + WebdavUtils.encodePath(path),
                    propSet,
                    0);
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
        } finally {
            if (propfind != null) {
                propfind.releaseConnection();
            }
        }

        return new RemoteOperationResult(RemoteOperationResult.ResultCode.ETAG_CHANGED);
    }
}
