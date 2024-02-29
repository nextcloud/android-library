/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2014-2016 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-FileCopyrightText: 2014-2016 ownCloud Inc.
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common;

public class OwnCloudCredentialsFactory {
	public static final String CREDENTIAL_CHARSET = "UTF-8";

	private static OwnCloudAnonymousCredentials sAnonymousCredentials;

	public static OwnCloudCredentials newBasicCredentials(String username, String password) {
		return new OwnCloudBasicCredentials(username, password);
	}

	public static final OwnCloudCredentials getAnonymousCredentials() {
		if (sAnonymousCredentials == null) {
			sAnonymousCredentials = new OwnCloudAnonymousCredentials();
		}
		return sAnonymousCredentials;
	}
}
