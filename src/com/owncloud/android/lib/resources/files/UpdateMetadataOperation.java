/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2017 Tobias Kaminsky
 * Copyright (C) 2017 Nextcloud GmbH.
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

public class UpdateMetadataOperation extends RemoteOperation {

    private static final String TAG = UpdateMetadataOperation.class.getSimpleName();
    private static final int SYNC_READ_TIMEOUT = 40000;
    private static final int SYNC_CONNECTION_TIMEOUT = 5000;
    private static final String METADATA_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/meta-data/";

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
    public UpdateMetadataOperation(String fileId, String encryptedMetadataJson, String token) {
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
            putMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");

            NameValuePair[] putParams = new NameValuePair[2];
            putParams[0] = new NameValuePair("token", token);
            putParams[1] = new NameValuePair("format", "json");
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
            e.printStackTrace();
            Log_OC.e(TAG, "Storing of metadata for folder " + fileId + " failed: " +
                    result.getLogMessage(), result.getException());
        } finally {
            if (putMethod != null)
                putMethod.releaseConnection();
        }
        return result;
    }

}
