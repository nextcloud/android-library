/*
 * Nextcloud Android client application
 *
 * @author Barotsz Przybylski
 * @author Tobias Kaminsky
 * Copyright (C) 2018 Bartosz Przybylski
 * Copyright (C) 2019 Tobias Kaminsky
 * Copyright (C) 2019 Nextcloud GmbH
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

package com.owncloud.android.lib.resources.users;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.operations.PutMethod;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;

import okhttp3.MediaType;
import okhttp3.RequestBody;


public class SetUserInfoRemoteOperation extends RemoteOperation<Boolean> {

    private static final String TAG = SetUserInfoRemoteOperation.class.getSimpleName();

    private static final String OCS_ROUTE_PATH = "/ocs/v2.php/cloud/users/";

    public enum Field {
        EMAIL("email"),
        DISPLAYNAME("displayname"),
        PHONE("phone"),
        ADDRESS("address"),
        WEBSITE("website"),
        TWITTER("twitter");

        private final String fieldName;

        Field(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldName() {
            return fieldName;
        }
    }

    private final Field field;
    private final String value;

    public SetUserInfoRemoteOperation(Field field, String value) {
        this.field = field;
        this.value = value;
    }

    @Override
    public RemoteOperationResult<Boolean> run(NextcloudClient client) {
        RemoteOperationResult<Boolean> result;
        PutMethod method = null;

        try {
            // request body
            MediaType json = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(json, "{\"key\": \"" + field.getFieldName() + "\"" +
                    ", \"value\": \"" + value + "\"}");

            // remote request
            method = new PutMethod(client.getBaseUri() + OCS_ROUTE_PATH + client.getUserIdPlain(),
                    true,
                    requestBody);

            int status = client.execute(method);

            if (status == HttpStatus.SC_OK) {
                result = new RemoteOperationResult<>(true, method);

            } else {
                result = new RemoteOperationResult<>(false, method);
                String response = method.getResponseBodyAsString();
                Log_OC.e(TAG, "Failed response while setting user information");
                Log_OC.e(TAG, "*** status code: " + status + "; response: " + response);
            }
        } catch (Exception e) {
            result = new RemoteOperationResult<>(e);
            Log_OC.e(TAG, "Exception while setting OC user information", e);
        } finally {
            if (method != null)
                method.releaseConnection();
        }

        return result;
    }
}
