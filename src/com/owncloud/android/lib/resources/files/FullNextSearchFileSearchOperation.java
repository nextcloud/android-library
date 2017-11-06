/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2017 Tobias Kaminsky
 * Copyright (C) 2017 Nextcloud GmbH.
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

package com.owncloud.android.lib.resources.files;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Search operation for full next search
 */
public class FullNextSearchFileSearchOperation extends RemoteOperation {

    private static final String SEARCH_PATH = "/index.php/apps/fullnextsearch/v1/remote/files/";

    private String query;

    public FullNextSearchFileSearchOperation(String query) {
        this.query = query;
    }

    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result = null;
        GetMethod getMethod = new GetMethod(client.getBaseUri() + SEARCH_PATH);
        getMethod.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);
        getMethod.setQueryString("search="+query);

        try {
            int status = client.executeMethod(getMethod);

            if (status == HttpStatus.SC_CREATED) {
                
                String response = getMethod.getResponseBodyAsString();

                JsonParser jsonParser = new JsonParser();
                JsonObject jsonObject = (JsonObject) jsonParser.parse(response);
                JsonArray jsonDataArray = (JsonArray) jsonObject.getAsJsonArray("result").getAsJsonArray().get(0)
                        .getAsJsonObject().get("documents");

                HashMap<Integer, ArrayList<String>> results = new HashMap<>();
                for (JsonElement element: jsonDataArray) {
                    Integer id = element.getAsJsonObject().get("id").getAsInt();

                    ArrayList<String> excerpts = new ArrayList<>();
                    for (JsonElement excerpt: element.getAsJsonObject().get("excerpts").getAsJsonArray()) {
                        excerpts.add(excerpt.getAsString());
                    }                    
                    
                    results.put(id, excerpts);
                }
                result = new RemoteOperationResult(true, status, getMethod.getResponseHeaders());
                ArrayList<Object> list = new ArrayList<>();
                list.add(results);
                result.setData(list);
                
            } else {
                client.exhaustResponse(getMethod.getResponseBodyAsStream());
                result = new RemoteOperationResult(false, status, getMethod.getResponseHeaders());
            }
        } catch (IOException e) {
            result = new RemoteOperationResult(e);
        } finally {
            getMethod.releaseConnection();  // let the connection available for other methods
        }

        return result;
    }
}
