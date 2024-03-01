/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2018-2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.files.model;

public interface ServerFileInterface {

    String getFileName();

    String getMimeType();

    String getRemotePath();

    long getLocalId();

    String getRemoteId();

    boolean isFavorite();

    boolean isFolder();

    long getFileLength();
}
