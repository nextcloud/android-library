/* Nextcloud Android Library is available under MIT license
 *
 *   @author Bartosz Przybylski
 *   Copyright (C) 2018 Bartosz Przybylski
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

package com.owncloud.android.lib.resources;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.nextcloud.common.OkHttpMethodBase;
import com.owncloud.android.lib.common.operations.RemoteOperation;

import org.apache.commons.httpclient.HttpMethodBase;

import java.io.IOException;

/**
 *
 * Base class for OCS remote operations with convenient methods
 *
 * @author Bartosz Przybylski
 */
public abstract class OCSRemoteOperation extends RemoteOperation {

    @Deprecated
    public <T> T getServerResponse(HttpMethodBase method, TypeToken<T> type) throws IOException {
        String response = method.getResponseBodyAsString();
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(response);

        Gson gson = new Gson();

        return gson.fromJson(element, type.getType());
    }

    public <T> T getServerResponse(OkHttpMethodBase method, TypeToken<T> type) throws IOException {
        String response = method.getResponseBodyAsString();
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(response);

        Gson gson = new Gson();

        return gson.fromJson(element, type.getType());
    }
}
