/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2019 Tobias Kaminsky
 *   Copyright (C) 2019 Nextcloud GmbH
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

package com.nextcloud.android.lib.resources.directediting;

import com.google.gson.reflect.TypeToken;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.TemplateList;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.ocs.ServerResponse;
import com.owncloud.android.lib.resources.OCSRemoteOperation;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import lombok.AllArgsConstructor;

/**
 * Get templates for an editor
 */

@AllArgsConstructor
public class DirectEditingObtainListOfTemplatesRemoteOperation extends OCSRemoteOperation {
    private static final String TAG = DirectEditingObtainListOfTemplatesRemoteOperation.class.getSimpleName();
    private static final int SYNC_READ_TIMEOUT = 40000;
    private static final int SYNC_CONNECTION_TIMEOUT = 5000;
    private static final String DIRECT_ENDPOINT = "/ocs/v2.php/apps/files/api/v1/directEditing/templates/";

    private static final String JSON_FORMAT = "?format=json";

    private String editor;
    private String template;

    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result;
        GetMethod getMethod = null;

        try {
            getMethod = new GetMethod(client.getBaseUri() + DIRECT_ENDPOINT + editor + "/" + template + JSON_FORMAT);

            // remote request
            getMethod.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);

            int status = client.executeMethod(getMethod, SYNC_READ_TIMEOUT, SYNC_CONNECTION_TIMEOUT);

            if (status == HttpStatus.SC_OK) {
                TemplateList templateList = getServerResponse(getMethod,
                                                              new TypeToken<ServerResponse<TemplateList>>() {
                                                              })
                        .getOcs().getData();

                result = new RemoteOperationResult(true, getMethod);
                result.setSingleData(templateList);
            } else {
                result = new RemoteOperationResult(false, getMethod);
                client.exhaustResponse(getMethod.getResponseBodyAsStream());
            }
        } catch (Exception e) {
            result = new RemoteOperationResult(e);
            Log_OC.e(TAG, "Get all direct editing informations failed: " + result.getLogMessage(),
                     result.getException());
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
        return result;
    }
}
