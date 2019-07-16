package com.owncloud.android.lib.common;

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
    public String getUsername() {
        // no user name
        return null;
    }
}
