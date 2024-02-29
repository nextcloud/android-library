/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019-2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2020 Chris Narkiewicz <hello@ezaquarii.com>
 * SPDX-FileCopyrightText: 2014-2015 ownCloud Inc.
 * SPDX-FileCopyrightText: 2014 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.common;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.nextcloud.common.NextcloudClient;
import com.owncloud.android.lib.common.accounts.AccountUtils;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.cookie.CookiePolicy;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 
 * @author David A. Velasco
 * @author masensio
 * @author Tobias Kaminsky
 */

public class OwnCloudClientManager {

    private static final String TAG = OwnCloudClientManager.class.getSimpleName();

    private ConcurrentMap<String, OwnCloudClient> mClientsWithKnownUsername = new ConcurrentHashMap<>();
    private ConcurrentMap<String, NextcloudClient> clientsNewWithKnownUsername = new ConcurrentHashMap<>();

    private ConcurrentMap<String, OwnCloudClient> mClientsWithUnknownUsername = new ConcurrentHashMap<>();
    private ConcurrentMap<String, NextcloudClient> clientsNewWithUnknownUsername = new ConcurrentHashMap<>();

    @Deprecated
    public OwnCloudClient getClientFor(OwnCloudAccount account, Context context) throws OperationCanceledException,
            AuthenticatorException, IOException {

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log_OC.d(TAG, "getClientFor starting ");
        }
        if (account == null) {
            throw new IllegalArgumentException("Cannot get an OwnCloudClient for a null account");
        }

        OwnCloudClient client = null;
        String accountName = account.getName();
        String sessionName = account.getCredentials() == null ? "" :
                AccountUtils.buildAccountName(account.getBaseUri(), account.getCredentials().getAuthToken());

        if (accountName != null) {
            client = mClientsWithKnownUsername.get(accountName);
        }
        boolean reusingKnown = false;    // just for logs
        if (client == null) {
            if (accountName != null) {
                client = mClientsWithUnknownUsername.remove(sessionName);
                if (client != null) {
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log_OC.v(TAG, "reusing client for session " + sessionName);
                    }
                    mClientsWithKnownUsername.put(accountName, client);
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log_OC.v(TAG, "moved client to account " + accountName);
                    }
                }
            } else {
                client = mClientsWithUnknownUsername.get(sessionName);
            }
        } else {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log_OC.v(TAG, "reusing client for account " + accountName);
            }
            reusingKnown = true;
        }

        if (client == null) {
            // no client to reuse - create a new one
            // TODO remove dependency on OwnCloudClientFactory
            client = OwnCloudClientFactory.createOwnCloudClient(account.getBaseUri(), context.getApplicationContext(),
                    true);
            client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            // enable cookie tracking

            AccountUtils.restoreCookies(accountName, client, context);

            account.loadCredentials(context);
            client.setCredentials(account.getCredentials());

            AccountManager accountManager = AccountManager.get(context);
            Account savedAccount = account.getSavedAccount();

            if (savedAccount != null) {
                String userId = accountManager.getUserData(account.getSavedAccount(),
                        AccountUtils.Constants.KEY_USER_ID);
                client.setUserId(userId);
            }

            if (accountName != null) {
                mClientsWithKnownUsername.put(accountName, client);
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log_OC.v(TAG, "new client for account " + accountName);
                }

            } else {
                mClientsWithUnknownUsername.put(sessionName, client);
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log_OC.v(TAG, "new client for session " + sessionName);
                }
            }
        } else {
            if (!reusingKnown && Log.isLoggable(TAG, Log.VERBOSE)) {
                Log_OC.v(TAG, "reusing client for session " + sessionName);
            }
            keepCredentialsUpdated(account, client);
            keepUriUpdated(account, client);
        }

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log_OC.d(TAG, "getClientFor finishing ");
        }

        return client;
    }

    public NextcloudClient getNextcloudClientFor(OwnCloudAccount account, Context context)
            throws OperationCanceledException, AuthenticatorException, IOException {

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log_OC.d(TAG, "getClientFor starting");
        }
        if (account == null) {
            throw new IllegalArgumentException("Cannot get an NextcloudClient for a null account");
        }

        NextcloudClient client = null;
        String accountName = account.getName();
        String sessionName = account.getCredentials() == null ? "" :
                AccountUtils.buildAccountName(account.getBaseUri(), account.getCredentials().getAuthToken());

        if (accountName != null) {
            client = clientsNewWithKnownUsername.get(accountName);
        }
        boolean reusingKnown = false;    // just for logs
        if (client == null) {
            if (accountName != null) {
                client = clientsNewWithUnknownUsername.remove(sessionName);
                if (client != null) {
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log_OC.v(TAG, "reusing client for session " + sessionName);
                    }
                    clientsNewWithKnownUsername.put(accountName, client);
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log_OC.v(TAG, "moved client to account " + accountName);
                    }
                }
            } else {
                client = clientsNewWithUnknownUsername.get(sessionName);
            }
        } else {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log_OC.v(TAG, "reusing client for account " + accountName);
            }
            reusingKnown = true;
        }

        if (client == null) {


            // TODO v2
            //client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            // enable cookie tracking

            //AccountUtils.restoreCookies(accountName, client, context);

            account.loadCredentials(context);
            String credentials = account.getCredentials().toOkHttpCredentials();

            AccountManager accountManager = AccountManager.get(context);
            Account savedAccount = account.getSavedAccount();

            String userId;
            if (savedAccount != null) {
                userId = accountManager.getUserData(account.getSavedAccount(),
                        AccountUtils.Constants.KEY_USER_ID);
            } else {
                userId = "";
            }

            // no client to reuse - create a new one
            // TODO remove dependency on OwnCloudClientFactory
            client = OwnCloudClientFactory.createNextcloudClient(account.getBaseUri(),
                    userId,
                    credentials,
                    context.getApplicationContext(),
                    true);

            if (accountName != null) {
                clientsNewWithKnownUsername.put(accountName, client);
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log_OC.v(TAG, "new client for account " + accountName);
                }

            } else {
                clientsNewWithUnknownUsername.put(sessionName, client);
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log_OC.v(TAG, "new client for session " + sessionName);
                }
            }
        } else {
            if (!reusingKnown && Log.isLoggable(TAG, Log.VERBOSE)) {
                Log_OC.v(TAG, "reusing client for session " + sessionName);
            }
            // TODO v2
            // keepCredentialsUpdated(account, client);
            // keepUriUpdated(account, client);
        }

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log_OC.d(TAG, "getClientFor finishing ");
        }

        return client;
    }


    public OwnCloudClient removeClientFor(OwnCloudAccount account) {

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log_OC.d(TAG, "removeClientFor starting ");
        }

        if (account == null) {
            return null;
        }

        OwnCloudClient client;
        String accountName = account.getName();
        if (accountName != null) {
            client = mClientsWithKnownUsername.remove(accountName);
            if (client != null) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log_OC.v(TAG, "Removed client for account " + accountName);
                }
                return client;
            } else {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log_OC.v(TAG, "No client tracked for  account " + accountName);
                }
            }
        }

        mClientsWithUnknownUsername.clear();

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log_OC.d(TAG, "removeClientFor finishing ");
        }
        return null;

    }


    public void saveAllClients(Context context, String accountType) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log_OC.d(TAG, "Saving sessions... ");
        }

        Iterator<Map.Entry<String, OwnCloudClient>> accountNames = mClientsWithKnownUsername.entrySet().iterator();
        Map.Entry<String, OwnCloudClient> entry;
        Account account;
        while (accountNames.hasNext()) {
            entry = accountNames.next();
            account = new Account(entry.getKey(), accountType);
            AccountUtils.saveClient(
                    entry.getValue(),
                    account,
                    context);
        }

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log_OC.d(TAG, "All sessions saved");
        }
    }


    private void keepCredentialsUpdated(OwnCloudAccount account, OwnCloudClient reusedClient) {
        OwnCloudCredentials recentCredentials = account.getCredentials();
        if (recentCredentials != null && !recentCredentials.getAuthToken().equals(
                reusedClient.getCredentials().getAuthToken())) {
            reusedClient.setCredentials(recentCredentials);
        }

    }

    // this method is just a patch; we need to distinguish accounts in the same host but
    // different paths; but that requires updating the accountNames for apps upgrading 
    private void keepUriUpdated(OwnCloudAccount account, OwnCloudClient reusedClient) {
        Uri recentUri = account.getBaseUri();
        if (!recentUri.equals(reusedClient.getBaseUri())) {
            reusedClient.setBaseUri(recentUri);
        }

    }


}
