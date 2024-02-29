/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019-2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2018 Bartosz Przybylski <bart.p.pl@gmail.com>
 * SPDX-License-Identifier: MIT
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
 * Base class for OCS remote operations with convenient methods
 *
 * @author Bartosz Przybylski
 */
public abstract class OCSRemoteOperation<T> extends RemoteOperation<T> {

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
