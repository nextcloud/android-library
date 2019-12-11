package com.owncloud.android.lib.common;

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
        return Credentials.basic(getUsername(), getAuthToken());
    }

    @Override
    public String getUsername() {
        // no user name
        return null;
    }
}
