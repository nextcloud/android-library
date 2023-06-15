/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2023 Tobias Kaminsky
 * Copyright (C) 2023 Nextcloud GmbH
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 *  
 */

package com.owncloud.android.lib.resources;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.nextcloud.common.OkHttpMethodBase;
import com.owncloud.android.lib.common.operations.NextcloudRemoteOperation;

import org.apache.commons.httpclient.HttpMethodBase;

import java.io.IOException;

/**
 * Base class for OCS remote operations with convenient methods
 *
 * @author Bartosz Przybylski
 */
public abstract class OCSNextcloudRemoteOperation<T> extends NextcloudRemoteOperation<T> {

    @Deprecated
    public <T> T getServerResponse(HttpMethodBase method, TypeToken<T> type) throws IOException {
        String response = method.getResponseBodyAsString();
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(response);

        Gson gson = new Gson();

        return gson.fromJson(element, type.getType());
    }

    public <T> T getServerResponse(OkHttpMethodBase method, TypeToken<T> type) {
        String response = method.getResponseBodyAsString();
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(response);

        Gson gson = new Gson();

        return gson.fromJson(element, type.getType());
    }
}
