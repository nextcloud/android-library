/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2022 Tobias Kaminsky
 *   Copyright (C) 2022 Nextcloud GmbH
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

package com.nextcloud.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class NextcloudAuthenticator implements Authenticator {
    private String credentials;
    private String authenticatorType;

    public NextcloudAuthenticator(@NonNull String credentials, @NonNull String authenticatorType) {
        this.credentials = credentials;
        this.authenticatorType = authenticatorType;
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NonNull Response response) {
        if (response.request().header(authenticatorType) != null) {
            return null;
        }

        Response countedResponse = response;

        int attemptsCount = 0;

        while ((countedResponse = countedResponse.priorResponse()) != null) {
            attemptsCount++;
            if (attemptsCount == 3) {
                return null;
            }
        }

        return response.request().newBuilder()
                .header(authenticatorType, credentials)
                .build();
    }
}
