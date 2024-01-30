/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.e2ee;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.GetMethod;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONObject;


/**
 * Remote operation performing the fetch of metadata for a folder
 */

public class GetMetadataRemoteOperation extends RemoteOperation<MetadataResponse> {

    private static final String TAG = GetMetadataRemoteOperation.class.getSimpleName();
    private static final String METADATA_V1_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v1/meta-data/";
    private static final String METADATA_V2_URL = "/ocs/v2.php/apps/end_to_end_encryption/api/v2/meta-data/";

    // JSON node names
    private static final String NODE_OCS = "ocs";
    private static final String NODE_DATA = "data";
    private static final String NODE_META_DATA = "meta-data";
    private static final String HEADER_SIGNATURE = "X-NC-E2EE-SIGNATURE";

    private final long fileId;

    /**
     * Constructor
     */
    public GetMetadataRemoteOperation(long fileId) {
        this.fileId = fileId;
    }

    /**
     * @param client Client object
     */
    @Override
    public RemoteOperationResult<MetadataResponse> run(NextcloudClient client) {
        com.nextcloud.operations.GetMethod getMethod = null;
        RemoteOperationResult<MetadataResponse> result;

        try {
            // remote request
            getMethod = new com.nextcloud.operations.GetMethod(client.getBaseUri() + METADATA_V2_URL + fileId + JSON_FORMAT, true);

            int status = client.execute(getMethod);

            if (status == HttpStatus.SC_NOT_FOUND || status == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                // retry with v1
                getMethod = new GetMethod(client.getBaseUri() + METADATA_V1_URL + fileId + JSON_FORMAT, true);

                status = client.execute(getMethod);
            }

            if (status == HttpStatus.SC_OK) {
                String response = getMethod.getResponseBodyAsString();
                String signature = getMethod.getResponseHeader(HEADER_SIGNATURE);

                if (signature == null) {
                    signature = "";
                }

                // Parse the response
                JSONObject respJSON = new JSONObject(response);
                String metadata = respJSON
                        .getJSONObject(NODE_OCS)
                        .getJSONObject(NODE_DATA)
                        .getString(NODE_META_DATA);

                MetadataResponse metadataResponse = new MetadataResponse(signature, metadata);

                result = new RemoteOperationResult<>(true, getMethod);
                result.setResultData(metadataResponse);
            } else {
                result = new RemoteOperationResult<>(false, getMethod);
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Fetching of metadata for folder " + fileId + " failed: " +
                    result.getLogMessage(), result.getException());
        } finally {
            if (getMethod != null)
                getMethod.releaseConnection();
        }
        return result;
    }

}
