/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019-2024 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.directediting;

import com.google.gson.reflect.TypeToken;
import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.GetMethod;
import com.owncloud.android.lib.common.TemplateList;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.ocs.ServerResponse;
import com.owncloud.android.lib.resources.OCSRemoteOperation;

import org.apache.commons.httpclient.HttpStatus;

/**
 * Get templates for an editor
 */

public class DirectEditingObtainListOfTemplatesRemoteOperation extends OCSRemoteOperation<TemplateList> {
    private static final String TAG = DirectEditingObtainListOfTemplatesRemoteOperation.class.getSimpleName();
    private static final String DIRECT_ENDPOINT = "/ocs/v2.php/apps/files/api/v1/directEditing/templates/";

    private final String editor;
    private final String template;

    public DirectEditingObtainListOfTemplatesRemoteOperation(String editor, String template) {
        this.editor = editor;
        this.template = template;
    }

    public RemoteOperationResult<TemplateList> run(NextcloudClient client) {
        RemoteOperationResult<TemplateList> result;
        com.nextcloud.operations.GetMethod get = null;

        try {
            // get request
            get = new GetMethod(client.getBaseUri() + DIRECT_ENDPOINT + editor + "/" + template + JSON_FORMAT, true);

            int status = client.execute(get);

            if (status == HttpStatus.SC_OK) {
                ServerResponse<TemplateList> serverResponse = getServerResponse(get, new TypeToken<>() {});

                if (serverResponse != null) {
                    TemplateList templateList = serverResponse.getOcs().getData();
                    result = new RemoteOperationResult<>(true, get);
                    result.setResultData(templateList);
                } else {
                    result = new RemoteOperationResult<>(false, get);
                }
            } else {
                result = new RemoteOperationResult<>(false, get);
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Get all direct editing information failed: " + result.getLogMessage(),
                     result.getException());
        } finally {
            if (get != null) {
                get.releaseConnection();
            }
        }

        return result;
    }
}
