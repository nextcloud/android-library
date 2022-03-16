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

package com.owncloud.android.lib.common.accounts;

import com.owncloud.android.lib.common.ExternalLink;
import com.owncloud.android.lib.common.ExternalLinkType;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.status.GetCapabilitiesRemoteOperation;
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
    private static final String NODE_REDIRECT = "redirect";


    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result = null;
        int status = -1;
        GetMethod get = null;
        String ocsUrl = client.getBaseUri() + OCS_ROUTE_EXTERNAL_LINKS;

        try {
            // check capabilities
            RemoteOperation getCapabilities = new GetCapabilitiesRemoteOperation();
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

                            boolean redirect = false;

                            if (link.has(NODE_REDIRECT)) {
                                redirect = link.getInt(NODE_REDIRECT) == 1;
                            }

                            resultLinks.add(new ExternalLink(id, iconUrl, language, type, name, url, redirect));
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
