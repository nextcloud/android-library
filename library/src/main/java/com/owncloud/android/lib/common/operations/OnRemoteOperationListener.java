/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2014-2015 ownCloud Inc.
 * SPDX-FileCopyrightText: 2014 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.operations;


public interface OnRemoteOperationListener {

	void onRemoteOperationFinish(RemoteOperation caller, RemoteOperationResult result);
	
}
