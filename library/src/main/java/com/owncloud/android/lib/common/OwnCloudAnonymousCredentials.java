/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2019 Chris Narkiewicz <hello@ezaquarii.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common;

import android.os.Parcel;

import java.nio.charset.StandardCharsets;

import okhttp3.Credentials;

public class OwnCloudAnonymousCredentials implements OwnCloudCredentials {

    protected OwnCloudAnonymousCredentials() {
    }

    @Override
    public void applyTo(OwnCloudClient client) {
        client.getState().clearCredentials();
        client.getState().clearCookies();
    }

    @Override
    public String getAuthToken() {
        return "";
    }

    @Override
    public boolean authTokenExpires() {
        return false;
    }

    @Override
    public String toOkHttpCredentials() {
        return Credentials.basic(getUsername(), getAuthToken(), StandardCharsets.UTF_8);
    }

    @Override
    public String getUsername() {
        // no user name
        return null;
    }

    /*
     * Autogenerated Parcelable interface
     */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    protected OwnCloudAnonymousCredentials(Parcel in) {
    }

    public static final Creator<OwnCloudAnonymousCredentials> CREATOR = new Creator<OwnCloudAnonymousCredentials>() {
        @Override
        public OwnCloudAnonymousCredentials createFromParcel(Parcel source) {
            return new OwnCloudAnonymousCredentials(source);
        }

        @Override
        public OwnCloudAnonymousCredentials[] newArray(int size) {
            return new OwnCloudAnonymousCredentials[size];
        }
    };
}
