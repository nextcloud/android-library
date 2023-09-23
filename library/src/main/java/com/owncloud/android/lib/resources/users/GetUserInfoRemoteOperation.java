/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2017 Tobias Kaminsky
 *   Copyright (C) 2017 Nextcloud GmbH
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

package com.owncloud.android.lib.resources.users;

import com.google.gson.reflect.TypeToken;
import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.GetMethod;
import com.owncloud.android.lib.common.Quota;
import com.owncloud.android.lib.common.UserInfo;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.ocs.ServerResponse;
import com.owncloud.android.lib.resources.OCSNextcloudRemoteOperation;

import org.apache.commons.httpclient.HttpStatus;

import java.util.HashMap;

/**
 * Gets information (id, display name, and e-mail address and many other things) about the user logged in.
 *
 * @author masensio
 * @author David A. Velasco
 * @author Mario Danic
 */
public class GetUserInfoRemoteOperation extends OCSNextcloudRemoteOperation<UserInfo> {

    private static final String TAG = GetUserInfoRemoteOperation.class.getSimpleName();

    // OCS Route
    private static final String OCS_ROUTE_SELF = "/ocs/v2.php/cloud/user";

    /**
     * Quota return value for a not computed space value.
     */
    public static final long SPACE_NOT_COMPUTED = -1;

    /**
     * Quota return value for unknown space value.
     */
    public static final long SPACE_UNKNOWN = -2;

    /**
     * Quota return value for unlimited space.
     */
    public static final long SPACE_UNLIMITED = -3;

    /**
     * Quota return value for quota information not available.
     */
    public static final long QUOTA_LIMIT_INFO_NOT_AVAILABLE = Long.MIN_VALUE;

    @Override
    public RemoteOperationResult<UserInfo> run(NextcloudClient client) {
        RemoteOperationResult<UserInfo> result;
        int status;
        GetMethod get = null;

        String url = client.getBaseUri() + OCS_ROUTE_SELF;

        // get the user
        try {

            get = new GetMethod(url, true);
            HashMap<String, String> map = new HashMap<>();
            map.put("format", "json");

            get.setQueryString(map);
            status = client.execute(get);

            if (isSuccess(status)) {
                ServerResponse<UserInfo> ocsResponse = getServerResponse(get,
                        new TypeToken<ServerResponse<UserInfo>>() {
                        });

                UserInfo userInfo = ocsResponse.ocs.data;

                if (userInfo.getQuota() == null || userInfo.getQuota().getQuota() == 0) {
                    userInfo.setQuota(new Quota(QUOTA_LIMIT_INFO_NOT_AVAILABLE));
                }

                result = new RemoteOperationResult<>(true, get);
                result.setResultData(userInfo);
            } else {
                result = new RemoteOperationResult<>(false, get);
                String response = get.getResponseBodyAsString();
                Log_OC.e(TAG, "Failed response while getting user information");
                if (response.isEmpty()) {
                    Log_OC.e(TAG, "*** status code: " + status);
                } else {
                    Log_OC.e(TAG, "*** status code: " + status + "; response message: " + response);
                }
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Exception while getting OC user information", e);
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
