/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2014-2016 ownCloud Inc. and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017-2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2017 Mario Danic <mario@lovelyhq.com>
 * SPDX-FileCopyrightText: 2014-2016 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-FileCopyrightText: 2014-2015 masensio <masensio@solidgear.es>
 * SPDX-FileCopyrightText: 2012 Bartosz Przybylski <bart.p.pl@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountsException;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.net.Uri;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudCredentials;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.status.OwnCloudVersion;

import org.apache.commons.httpclient.Cookie;

import java.io.IOException;

public class AccountUtils {

    private static final String TAG = AccountUtils.class.getSimpleName();

    public static final String WEBDAV_PATH_9_0 = "/remote.php/dav";
    public static final String DAV_UPLOAD = "/remote.php/dav/uploads";
    public static final String STATUS_PATH = "/status.php";

    /**
     * Extracts url server from the account
     *
     * @param context
     * @param account
     * @return url server or null on failure
     * @throws AccountNotFoundException When 'account' is unknown for the AccountManager
     * @deprecated This method will be removed in version 1.0.
     * Use {@link #getBaseUrlForAccount(Context, Account)}
     * instead.
     */
    @Deprecated
    public static String constructBasicURLForAccount(Context context, Account account)
            throws AccountNotFoundException {
        return getBaseUrlForAccount(context, account);
    }

    /**
     * Extracts url server from the account
     *
     * @param context
     * @param account
     * @return url server or null on failure
     * @throws AccountNotFoundException When 'account' is unknown for the AccountManager
     */
    public static String getBaseUrlForAccount(Context context, Account account)
            throws AccountNotFoundException {
        AccountManager ama = AccountManager.get(context.getApplicationContext());
        String baseurl = ama.getUserData(account, Constants.KEY_OC_BASE_URL);

        if (baseurl == null)
            throw new AccountNotFoundException(account, "Account not found", null);

        return baseurl;
    }


    /**
     * Get the username corresponding to an OC account.
     *
     * @param account An OC account
     * @return Username for the given account, extracted from the account.name
     */
    public static String getUsernameForAccount(Account account) {
        String username = null;
        try {
            username = account.name.substring(0, account.name.lastIndexOf('@'));
        } catch (Exception e) {
            Log_OC.e(TAG, "Couldn't get a username for the given account", e);
        }
        return username;
    }

    /**
     * Get the stored server version corresponding to an OC account.
     *
     * @param account An OC account
     * @param context Application context
     * @return Version of the OC server, according to last check
     */
    public static OwnCloudVersion getServerVersionForAccount(Account account, Context context) {
        AccountManager ama = AccountManager.get(context);
        OwnCloudVersion version = null;
        try {
            String versionString = ama.getUserData(account, Constants.KEY_OC_VERSION);
            version = new OwnCloudVersion(versionString);

        } catch (Exception e) {
            Log_OC.e(TAG, "Couldn't get a the server version for an account", e);
        }
        return version;
    }

    /**
     * @return
     * @throws IOException
     * @throws AuthenticatorException
     * @throws OperationCanceledException
     */
    public static OwnCloudCredentials getCredentialsForAccount(Context context, Account account)
            throws OperationCanceledException, AuthenticatorException, IOException {
        AccountManager am = AccountManager.get(context);

        String username = AccountUtils.getUsernameForAccount(account);

        String password = am.blockingGetAuthToken(account, AccountTypeUtils.getAuthTokenTypePass(account.type),
                false);

        return OwnCloudCredentialsFactory.newBasicCredentials(username, password);
    }


    public static String buildAccountNameOld(Uri serverBaseUrl, String username) {
        if (serverBaseUrl.getScheme() == null) {
            serverBaseUrl = Uri.parse("https://" + serverBaseUrl.toString());
        }
        String accountName = username + "@" + serverBaseUrl.getHost();
        if (serverBaseUrl.getPort() >= 0) {
            accountName += ":" + serverBaseUrl.getPort();
        }
        return accountName;
    }

    public static String buildAccountName(Uri serverBaseUrl, String loginName) {
        if (serverBaseUrl.getScheme() == null) {
            serverBaseUrl = Uri.parse("https://" + serverBaseUrl.toString());
        }

        // Remove http:// or https://
        String url = serverBaseUrl.toString();
        if (url.contains("://")) {
            url = url.substring(serverBaseUrl.toString().indexOf("://") + 3);
        }

        return loginName + "@" + url;
    }

    public static void saveClient(OwnCloudClient client, Account savedAccount, Context context) {

        // Account Manager
        AccountManager ac = AccountManager.get(context.getApplicationContext());

        if (client != null) {
            String cookiesString = client.getCookiesString();
            if (!"".equals(cookiesString)) {
                ac.setUserData(savedAccount, Constants.KEY_COOKIES, cookiesString);
                // Log_OC.d(TAG, "Saving Cookies: "+ cookiesString );
            }
        }

    }


    /**
     * Restore the client cookies
     *
     * @param account
     * @param client
     * @param context
     */
    public static void restoreCookies(Account account, OwnCloudClient client, Context context) {

        Log_OC.d(TAG, "Restoring cookies for " + account.name);

        // Account Manager
        AccountManager am = AccountManager.get(context.getApplicationContext());

        Uri serverUri = (client.getBaseUri() != null) ? client.getBaseUri() : client.getDavUri();

        String cookiesString = null;
        try {
            cookiesString = am.getUserData(account, Constants.KEY_COOKIES);
        } catch (SecurityException e) {
            Log_OC.e(TAG, e.getMessage());
        }

        if (cookiesString != null) {
            String[] cookies = cookiesString.split(";");
            if (cookies.length > 0) {
                for (int i = 0; i < cookies.length; i++) {
                    Cookie cookie = new Cookie();
                    int equalPos = cookies[i].indexOf('=');
                    cookie.setName(cookies[i].substring(0, equalPos));
                    cookie.setValue(cookies[i].substring(equalPos + 1));
                    cookie.setDomain(serverUri.getHost());    // VERY IMPORTANT
                    cookie.setPath(serverUri.getPath());    // VERY IMPORTANT

                    client.getState().addCookie(cookie);
                }
            }
        }
    }

    /**
     * Restore the client cookies from accountName
     *
     * @param accountName
     * @param client
     * @param context
     */
    public static void restoreCookies(String accountName, OwnCloudClient client, Context context) {
        Log_OC.d(TAG, "Restoring cookies for " + accountName);

        // Account Manager
        AccountManager am = AccountManager.get(context.getApplicationContext());

        // Get account
        Account account = null;
        Account accounts[] = am.getAccounts();
        for (Account a : accounts) {
            if (a.name.equals(accountName)) {
                account = a;
                break;
            }
        }

        // Restoring cookies
        if (account != null) {
            restoreCookies(account, client, context);
        }
    }

    public static class AccountNotFoundException extends AccountsException {

        /**
         * Generated - should be refreshed every time the class changes!!
         */
        private static final long serialVersionUID = -1684392454798508693L;

        private Account mFailedAccount;

        public AccountNotFoundException(Account failedAccount, String message, Throwable cause) {
            super(message, cause);
            mFailedAccount = failedAccount;
        }

        public Account getFailedAccount() {
            return mFailedAccount;
        }
    }


    public static class Constants {
        /**
         * Value under this key should handle path to webdav php script. Will be
         * removed and usage should be replaced by combining
         * {@link com.owncloud.android.authentication.AuthenticatorActivity.KEY_OC_BASE_URL} and
         * {@link com.owncloud.android.lib.resources.status.OwnCloudVersion}
         *
         * @deprecated
         */
        public static final String KEY_OC_URL = "oc_url";
        /**
         * Version should be 3 numbers separated by dot so it can be parsed by
         * {@link com.owncloud.android.lib.resources.status.OwnCloudVersion}
         */
        public static final String KEY_OC_VERSION = "oc_version";
        /**
         * Base url should point to owncloud installation without trailing / ie:
         * http://server/path or https://owncloud.server
         */
        public static final String KEY_OC_BASE_URL = "oc_base_url";
        /**
         * OC account cookies
         */
        public static final String KEY_COOKIES = "oc_account_cookies";

        /**
         * OC account version
         */
        public static final String KEY_OC_ACCOUNT_VERSION = "oc_account_version";

        /**
         * User's display name, name chosen by user, only displayed, no real usage
         */
        public static final String KEY_DISPLAY_NAME = "oc_display_name";

        /**
         * User ID, internally and never changing id of user, used by e.g. dav/trashbin/$userId/trash
         */
        public static final String KEY_USER_ID = "oc_id";
    }

}
