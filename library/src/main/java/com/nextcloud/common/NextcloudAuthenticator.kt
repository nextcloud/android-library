/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */

package com.nextcloud.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class NextcloudAuthenticator implements Authenticator {
    private final String credentials;

    public NextcloudAuthenticator(@NonNull String credentials) {
        this.credentials = credentials;
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NonNull Response response) {
        String authenticatorType = "Authorization";

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
