/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-FileCopyrightText: 2015 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.shares;

import static com.owncloud.android.lib.resources.shares.ShareUtils.INCLUDE_TAGS;
import static com.owncloud.android.lib.resources.shares.ShareUtils.INCLUDE_TAGS_OC;
import static com.owncloud.android.lib.resources.shares.ShareUtils.SHARING_API_PATH;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.GetMethod;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;

import java.util.List;

/**
 * Get the data about a Share resource, known its remote ID.
 */
public class GetShareRemoteOperation extends RemoteOperation<List<OCShare>> {

    private static final String TAG = GetShareRemoteOperation.class.getSimpleName();

    private final long remoteId;


    public GetShareRemoteOperation(long remoteId) {
        this.remoteId = remoteId;
    }

    @Override
    protected RemoteOperationResult<List<OCShare>> run(OwnCloudClient client) {
        RemoteOperationResult<List<OCShare>> result;
        int status;

        // Get Method
        org.apache.commons.httpclient.methods.GetMethod get = null;

        // Get the response
        try {
            get = new org.apache.commons.httpclient.methods.GetMethod(client.getBaseUri() + SHARING_API_PATH + "/" + remoteId);
            get.setQueryString(INCLUDE_TAGS_OC);
            get.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);

            status = client.executeMethod(get);

            if (isSuccess(status)) {
                String response = get.getResponseBodyAsString();

                // Parse xml response and obtain the list of shares
                ShareToRemoteOperationResultParser parser = new ShareToRemoteOperationResultParser(
                    new ShareXMLParser()
                );
                parser.setOneOrMoreSharesRequired(true);
                parser.setServerBaseUri(client.getBaseUri());
                result = parser.parse(response);

            } else {
                result = new RemoteOperationResult<>(false, get);
            }

        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Exception while getting remote shares ", e);

        } finally {
            if (get != null) {
                get.releaseConnection();
            }
        }
        return result;
    }

    @Override
    public RemoteOperationResult<List<OCShare>> run(NextcloudClient client) {
        RemoteOperationResult<List<OCShare>> result;
        int status;

        // get method
        com.nextcloud.operations.GetMethod get = null;

        try {
            get = new GetMethod(client.getBaseUri() + SHARING_API_PATH + "/" + remoteId, true);
            get.setQueryString(INCLUDE_TAGS);

            status = client.execute(get);

            if (isSuccess(status)) {
                String response = get.getResponseBodyAsString();

                // Parse xml response and obtain the list of shares
                ShareToRemoteOperationResultParser parser = new ShareToRemoteOperationResultParser(
                        new ShareXMLParser()
                );
                parser.setOneOrMoreSharesRequired(true);
                parser.setServerBaseUri(client.getBaseUri());
                result = parser.parse(response);

            } else {
                result = new RemoteOperationResult<>(false, get);
            }

        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Exception while getting remote shares ", e);

        } finally {
            if (get != null) {
                get.releaseConnection();
            }
        }
        return result;
    }

    private boolean isSuccess(int status) {
        return (status == HttpStatus.SC_OK);
    }


}
