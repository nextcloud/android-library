/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2021-2023 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-FileCopyrightText: 2014 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-FileCopyrightText: 2014 masensio <masensio@solidgear.es>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.shares;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.GetMethod;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;

import java.util.List;
import java.util.Map;

import static com.owncloud.android.lib.resources.shares.ShareUtils.INCLUDE_TAGS;

/**
 * Get the data from the server about ALL the known shares owned by the requester.
 */
public class GetSharesRemoteOperation extends RemoteOperation<List<OCShare>> {

    private static final String TAG = GetSharesRemoteOperation.class.getSimpleName();
    private final boolean sharedWithMe;

    public GetSharesRemoteOperation() {
        this(false);
    }

    public GetSharesRemoteOperation(boolean sharedWithMe) {
        this.sharedWithMe = sharedWithMe;
    }

    @Override
    public RemoteOperationResult<List<OCShare>> run(NextcloudClient client) {
        RemoteOperationResult<List<OCShare>> result;
        int status;

        // Get Method
        com.nextcloud.operations.GetMethod get = null;

        // Get the response
        try {
            get = new GetMethod(client.getBaseUri() + ShareUtils.SHARING_API_PATH, true);
            get.setQueryString(INCLUDE_TAGS);

            if (sharedWithMe) {
                get.setQueryString(Map.of("shared_with_me", "true"));
            }

            status = client.execute(get);

            if (isSuccess(status)) {
                String response = get.getResponseBodyAsString();

                // Parse xml response and obtain the list of shares
                ShareToRemoteOperationResultParser parser = new ShareToRemoteOperationResultParser(
                        new ShareXMLParser()
                );
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
