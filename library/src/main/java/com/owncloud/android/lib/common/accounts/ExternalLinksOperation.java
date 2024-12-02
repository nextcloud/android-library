/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017-2018 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.accounts;

import com.nextcloud.common.NextcloudClient;
import com.owncloud.android.lib.common.ExternalLink;
import com.owncloud.android.lib.common.ExternalLinkType;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.status.GetCapabilitiesRemoteOperation;
import com.owncloud.android.lib.resources.status.OCCapability;

import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * gets external links provided by 'external' app
 */

public class ExternalLinksOperation extends RemoteOperation<List<ExternalLink>> {

    private static final String TAG = ExternalLinksOperation.class.getSimpleName();

    // OCS Route
    private static final String OCS_ROUTE_EXTERNAL_LINKS = "/ocs/v2.php/apps/external/api/v1";

    // JSON Node names
    private static final String NODE_OCS = "ocs";
    private static final String NODE_DATA = "data";
    private static final String NODE_ID = "id";
    private static final String NODE_ICON = "icon";
    private static final String NODE_LANGUAGE = "lang";
    private static final String NODE_TYPE = "type";
    private static final String NODE_NAME = "name";
    private static final String NODE_URL = "url";
    private static final String NODE_REDIRECT = "redirect";


    @Override
    public RemoteOperationResult<List<ExternalLink>> run(NextcloudClient client) {
        RemoteOperationResult<List<ExternalLink>> result;
        int status;
        com.nextcloud.operations.GetMethod get = null;
        String ocsUrl = client.getBaseUri() + OCS_ROUTE_EXTERNAL_LINKS;

        try {
            // check capabilities
            RemoteOperation<OCCapability> getCapabilities = new GetCapabilitiesRemoteOperation();
            RemoteOperationResult<OCCapability> capabilitiesResult = getCapabilities.execute(client);
            OCCapability capability = capabilitiesResult.getResultData();

            if (capability != null && capability.getExternalLinks().isTrue()) {

                get = new com.nextcloud.operations.GetMethod(ocsUrl, true);
                get.setQueryString(Map.of("format", "json"));
                status = client.execute(get);

                if (isSuccess(status)) {
                    String response = get.getResponseBodyAsString();
                    Log_OC.d(TAG, "Successful response: " + response);

                    // parse
                    JSONArray links = new JSONObject(response).getJSONObject(NODE_OCS).getJSONArray(NODE_DATA);

                    ArrayList<ExternalLink> resultLinks = new ArrayList<>();

                    for (int i = 0; i < links.length(); i++) {
                        JSONObject link = links.getJSONObject(i);

                        if (link != null) {
                            int id = link.getInt(NODE_ID);
                            String iconUrl = link.getString(NODE_ICON);
                            String language = "";
                            if (link.has(NODE_LANGUAGE)) {
                                language = link.getString(NODE_LANGUAGE);
                            }

                            ExternalLinkType type = switch (link.getString(NODE_TYPE)) {
                                case "link" -> ExternalLinkType.LINK;
                                case "settings" -> ExternalLinkType.SETTINGS;
                                case "quota" -> ExternalLinkType.QUOTA;
                                default -> ExternalLinkType.UNKNOWN;
                            };


                            String name = link.getString(NODE_NAME);
                            String url = link.getString(NODE_URL);

                            boolean redirect = false;

                            if (link.has(NODE_REDIRECT)) {
                                redirect = link.getInt(NODE_REDIRECT) == 1;
                            }

                            resultLinks.add(new ExternalLink(id, iconUrl, language, type, name, url, redirect));
                        }
                    }

                    result = new RemoteOperationResult<>(true, get);
                    result.setResultData(resultLinks);

                } else {
                    result = new RemoteOperationResult<>(false, get);
                    String response = get.getResponseBodyAsString();
                    Log_OC.e(TAG, "Failed response while getting external links ");
                    Log_OC.e(TAG, "*** status code: " + status + " ; response message: " + response);
                }
            } else {
                result = new RemoteOperationResult<>(RemoteOperationResult.ResultCode.NOT_AVAILABLE);
                Log_OC.d(TAG, "External links disabled");
            }

        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Exception while getting external links ", e);
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
