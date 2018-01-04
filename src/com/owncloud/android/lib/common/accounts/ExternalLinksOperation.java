/**
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2017 Nextcloud GmbH.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.owncloud.android.lib.common.accounts;

import com.owncloud.android.lib.common.ExternalLink;
import com.owncloud.android.lib.common.ExternalLinkType;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.status.GetRemoteCapabilitiesOperation;
import com.owncloud.android.lib.resources.status.OCCapability;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * gets external links provided by 'external' app
 */

public class ExternalLinksOperation extends RemoteOperation {

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


    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result = null;
        int status = -1;
        GetMethod get = null;
        String ocsUrl = client.getBaseUri() + OCS_ROUTE_EXTERNAL_LINKS;

        try {
            // check capabilities
            RemoteOperation getCapabilities = new GetRemoteCapabilitiesOperation();
            RemoteOperationResult capabilitiesResult = getCapabilities.execute(client);
            OCCapability capability = (OCCapability) capabilitiesResult.getData().get(0);

            if (capability.getExternalLinks().isTrue()) {

                get = new GetMethod(ocsUrl);
                get.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);
                get.setQueryString(new NameValuePair[]{new NameValuePair("format", "json")});
                status = client.executeMethod(get);

                if (isSuccess(status)) {
                    String response = get.getResponseBodyAsString();
                    Log_OC.d(TAG, "Successful response: " + response);

                    // parse
                    JSONArray links = new JSONObject(response).getJSONObject(NODE_OCS).getJSONArray(NODE_DATA);

                    ArrayList<Object> resultLinks = new ArrayList<>();

                    for (int i = 0; i < links.length(); i++) {
                        JSONObject link = links.getJSONObject(i);

                        if (link != null) {
                            Integer id = link.getInt(NODE_ID);
                            String iconUrl = link.getString(NODE_ICON);
                            String language = "";
                            if (link.has(NODE_LANGUAGE)) {
                                language = link.getString(NODE_LANGUAGE);
                            }

                            ExternalLinkType type;
                            switch (link.getString(NODE_TYPE)) {
                                case "link":
                                    type = ExternalLinkType.LINK;
                                    break;
                                case "settings":
                                    type = ExternalLinkType.SETTINGS;
                                    break;
                                case "quota":
                                    type = ExternalLinkType.QUOTA;
                                    break;
                                default:
                                    type = ExternalLinkType.UNKNOWN;
                                    break;
                            }


                            String name = link.getString(NODE_NAME);
                            String url = link.getString(NODE_URL);

                            resultLinks.add(new ExternalLink(id, iconUrl, language, type, name, url));
                        }
                    }

                    result = new RemoteOperationResult(true, status, get.getResponseHeaders());
                    result.setData(resultLinks);

                } else {
                    result = new RemoteOperationResult(false, status, get.getResponseHeaders());
                    String response = get.getResponseBodyAsString();
                    Log_OC.e(TAG, "Failed response while getting external links ");
                    if (response != null) {
                        Log_OC.e(TAG, "*** status code: " + status + " ; response message: " + response);
                    } else {
                        Log_OC.e(TAG, "*** status code: " + status);
                    }
                }
            } else {
                result = new RemoteOperationResult(RemoteOperationResult.ResultCode.NOT_AVAILABLE);
                Log_OC.d(TAG, "External links disabled");
            }

        } catch (Exception e) {
            result = new RemoteOperationResult(e);
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
