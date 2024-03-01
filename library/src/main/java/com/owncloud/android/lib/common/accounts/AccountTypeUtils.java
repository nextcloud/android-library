/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2014-2016 ownCloud Inc. and Nextcloud contributors
 * SPDX-FileCopyrightText: 2014 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.accounts;

/**
 * @author masensio
 * @author David A. Velasco
 */
public class AccountTypeUtils {

    public static String getAuthTokenTypePass(String accountType) {
        return accountType + ".password";
    }
}
