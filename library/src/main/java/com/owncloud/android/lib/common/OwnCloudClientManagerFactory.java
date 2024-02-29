/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019-2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2014-2015 ownCloud Inc.
 * SPDX-FileCopyrightText: 2015 masensio <masensio@solidgear.es>
 * SPDX-FileCopyrightText: 2014 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common;

public class OwnCloudClientManagerFactory {
    private static OwnCloudClientManager sDefaultSingleton;
    private static String sUserAgent = "Mozilla/5.0 (Android) Nextcloud-android";
    private static String proxyHost = "";
    private static int proxyPort = -1;

    public static OwnCloudClientManager getDefaultSingleton() {
        if (sDefaultSingleton == null) {
            sDefaultSingleton = new OwnCloudClientManager();
        }
        return sDefaultSingleton;
    }

    public static void setUserAgent(String userAgent) {
        sUserAgent = userAgent;
    }

    public static String getUserAgent() {
        return sUserAgent;
    }

    public static void setProxyHost(String host) {
        proxyHost = host;
    }

    public static String getProxyHost() {
        return proxyHost;
    }

    public static void setProxyPort(int port) {
        proxyPort = port;
    }

    public static int getProxyPort() {
        return proxyPort;
    }
}
