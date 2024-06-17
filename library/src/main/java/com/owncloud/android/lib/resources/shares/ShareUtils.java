/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2015 ownCloud Inc.
 * SPDX-FileCopyrightText: 2014 masensio <masensio@solidgear.es>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.shares;

import java.util.Map;

/**
 * Contains Constants for Share Operation
 *
 * @author masensio
 *
 */

public class ShareUtils {

    // OCS Route
    public static final String SHARING_API_PATH = "/ocs/v2.php/apps/files_sharing/api/v1/shares";

    public static final String INCLUDE_TAGS_OC = "include_tags=true";
    public static final Map<String, String> INCLUDE_TAGS = Map.of("include_tags", "true");

    // String to build the link with the token of a share:
    public static final String SHARING_LINK_PATH_AFTER_VERSION_8 = "/index.php/s/";

    public static String getSharingLinkPath() {
        return SHARING_LINK_PATH_AFTER_VERSION_8;
    }

}
