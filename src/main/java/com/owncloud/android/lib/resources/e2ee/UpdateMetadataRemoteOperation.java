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

package com.owncloud.android.lib.resources.e2ee;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;


/**
 * Remote operation to update the folder metadata
 */

public class UpdateMetadataRemoteOperation extends RemoteOperation {

    private static final String TAG = UpdateMetadataRemoteOperation.class.getSimpleName();
    private static final int SYNC_READ_TIMEOUT = 40000;
    private static final int SYNC_CONNECTION_TIMEOUT = 5000;
    private static final String METADATA_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/meta-data/";
    private static final String FORMAT = "format";

    // JSON node names
    private static final String NODE_OCS = "ocs";
    private static final String NODE_DATA = "data";
    private static final String NODE_META_DATA = "meta-data";

    private String fileId;
    private String encryptedMetadataJson;
    private String token;

    /**
     * Constructor
     */
    public UpdateMetadataRemoteOperation(String fileId, String encryptedMetadataJson, String token) {
        this.fileId = fileId;
        this.encryptedMetadataJson = URLEncoder.encode(encryptedMetadataJson);
        this.token = token;
    }

    /**
     * @param client Client object
     */
    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        PutMethod putMethod = null;
        RemoteOperationResult result;

        try {
            // remote request
            putMethod = new PutMethod(client.getBaseUri() + METADATA_URL + fileId);
            putMethod.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);
            putMethod.addRequestHeader(CONTENT_TYPE, FORM_URLENCODED);

            NameValuePair[] putParams = new NameValuePair[2];
            putParams[0] = new NameValuePair(E2E_TOKEN, token);
            putParams[1] = new NameValuePair(FORMAT, "json");
            putMethod.setQueryString(putParams);

            StringRequestEntity data = new StringRequestEntity("metaData="+encryptedMetadataJson, 
                    "application/json", "UTF-8");
            putMethod.setRequestEntity(data);

            int status = client.executeMethod(putMethod, SYNC_READ_TIMEOUT, SYNC_CONNECTION_TIMEOUT);

            if (status == HttpStatus.SC_OK) {
                String response = putMethod.getResponseBodyAsString();

                // Parse the response
                JSONObject respJSON = new JSONObject(response);
                String metadata = (String) respJSON.getJSONObject(NODE_OCS).getJSONObject(NODE_DATA)
                        .get(NODE_META_DATA);

                result = new RemoteOperationResult(true, putMethod);
                ArrayList<Object> keys = new ArrayList<>();
                keys.add(metadata);
                result.setData(keys);
            } else {
                result = new RemoteOperationResult(false, putMethod);
                client.exhaustResponse(putMethod.getResponseBodyAsStream());
            }
        } catch (Exception e) {
            result = new RemoteOperationResult(e);
            Log_OC.e(TAG, "Storing of metadata for folder " + fileId + " failed: " + result.getLogMessage(),
                    result.getException());
        } finally {
            if (putMethod != null)
                putMethod.releaseConnection();
        }
        return result;
    }

}
