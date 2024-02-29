/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019 Chris Narkiewicz <hello@ezaquarii.com>
 * SPDX-FileCopyrightText: 2018 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2014 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-FileCopyrightText: 2014 ownCloud Inc.
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common;

import android.os.Parcelable;

public interface OwnCloudCredentials extends Parcelable {

    void applyTo(OwnCloudClient ownCloudClient);

    String getUsername();

    String getAuthToken();

    boolean authTokenExpires();

    String toOkHttpCredentials();
}
