/* ownCloud Android Library is available under MIT license
 *   Copyright (C) 2015 ownCloud Inc.
 *   
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *   
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *   
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 */

package com.owncloud.android.lib.common;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.nextcloud.common.NextcloudClient;
import com.owncloud.android.lib.common.accounts.AccountTypeUtils;
import com.owncloud.android.lib.common.accounts.AccountUtils;
import com.owncloud.android.lib.common.accounts.AccountUtils.AccountNotFoundException;
import com.owncloud.android.lib.common.network.NetworkUtils;
import com.owncloud.android.lib.common.utils.Log_OC;

import java.io.IOException;
import java.security.GeneralSecurityException;

import okhttp3.Credentials;
import okhttp3.Interceptor;

public class OwnCloudClientFactory {
    
    final private static String TAG = OwnCloudClientFactory.class.getSimpleName();
    
    /** Default timeout for waiting data from the server */
    public static final int DEFAULT_DATA_TIMEOUT = 60000;
    public static final long DEFAULT_DATA_TIMEOUT_LONG = 60000;
    
    /** Default timeout for establishing a connection */
    public static final int DEFAULT_CONNECTION_TIMEOUT = 60000;


    /**
     * Creates a OwnCloudClient setup for an ownCloud account
     * 
     * Do not call this method from the main thread.
     * 
     * @param account                       The ownCloud account
     * @param appContext                    Android application context
     * @return                              A OwnCloudClient object ready to be used
     * @throws AuthenticatorException       If the authenticator failed to get the authorization
     *                                      token for the account.
     * @throws OperationCanceledException   If the authenticator operation was cancelled while
     *                                      getting the authorization token for the account.
     * @throws IOException                  If there was some I/O error while getting the
     *                                      authorization token for the account.
     * @throws AccountNotFoundException     If 'account' is unknown for the AccountManager
     */
    public static OwnCloudClient createOwnCloudClient(Account account, Context appContext)
            throws OperationCanceledException, AuthenticatorException, IOException,
            AccountNotFoundException {
        //Log_OC.d(TAG, "Creating OwnCloudClient associated to " + account.name);
        Uri baseUri = Uri.parse(AccountUtils.getBaseUrlForAccount(appContext, account));
        AccountManager am = AccountManager.get(appContext);
        // TODO avoid calling to getUserData here
        String userId = am.getUserData(account, AccountUtils.Constants.KEY_USER_ID);

        OwnCloudClient client = createOwnCloudClient(baseUri, appContext, true);
        client.setUserId(userId);

        String username = AccountUtils.getUsernameForAccount(account);
        String password = am.blockingGetAuthToken(account, AccountTypeUtils.getAuthTokenTypePass(account.type), false);
        OwnCloudCredentials credentials = OwnCloudCredentialsFactory.newBasicCredentials(username, password);

        client.setCredentials(credentials);

        // Restore cookies
        AccountUtils.restoreCookies(account, client, appContext);
        
        return client;
    }


    public static OwnCloudClient createOwnCloudClient(Account account, Context appContext, Activity currentActivity)
            throws OperationCanceledException, AuthenticatorException, IOException,
            AccountNotFoundException {
        Uri baseUri = Uri.parse(AccountUtils.getBaseUrlForAccount(appContext, account));
        AccountManager am = AccountManager.get(appContext);
        // TODO avoid calling to getUserData here
        String userId = am.getUserData(account, AccountUtils.Constants.KEY_USER_ID);

        OwnCloudClient client = createOwnCloudClient(baseUri, appContext, true);
        client.setUserId(userId);

        String username = AccountUtils.getUsernameForAccount(account);
        //String password = am.getPassword(account);
        //String password = am.blockingGetAuthToken(account, MainApp.getAuthTokenTypePass(),
        // false);
        AccountManagerFuture<Bundle> future = am.getAuthToken(account,
                                                              AccountTypeUtils.getAuthTokenTypePass(account.type), null,
                                                              currentActivity, null, null);

        Bundle result = future.getResult();
        String password = result.getString(AccountManager.KEY_AUTHTOKEN);

        OwnCloudCredentials credentials = OwnCloudCredentialsFactory.newBasicCredentials(username, password);

        client.setCredentials(credentials);
        
        // Restore cookies
        AccountUtils.restoreCookies(account, client, appContext);
        
        return client;
    }
    
    /**
     * Creates a OwnCloudClient to access a URL and sets the desired parameters for ownCloud
     * client connections.
     * 
     * @param uri       URL to the ownCloud server; BASE ENTRY POINT, not WebDavPATH
     * @param context   Android context where the OwnCloudClient is being created.
     * @return          A OwnCloudClient object ready to be used
     */
    public static OwnCloudClient createOwnCloudClient(Uri uri, Context context, boolean followRedirects) {
        try {
            NetworkUtils.registerAdvancedSslContext(true, context);
        }  catch (GeneralSecurityException e) {
            Log_OC.e(TAG, "Advanced SSL Context could not be loaded. Default SSL management in" +
                    " the system will be used for HTTPS connections", e);

        } catch (IOException e) {
            Log_OC.e(TAG, "The local server truststore could not be read. Default SSL management" +
                    " in the system will be used for HTTPS connections", e);
        }

        OwnCloudClient client = new OwnCloudClient(uri, NetworkUtils.getMultiThreadedConnManager());
        client.setDefaultTimeouts(DEFAULT_DATA_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
        client.setFollowRedirects(followRedirects);

        return client;
    }

    public static NextcloudClient createNextcloudClient(Uri uri,
                                                        String userId,
                                                        String credentials,
                                                        Context context,
                                                        boolean followRedirects) {
        return createNextcloudClient(uri, userId, credentials, context, followRedirects, null);
    }

    /**
     * Creates a OwnCloudClient to access a URL and sets the desired parameters for ownCloud
     * client connections.
     *
     * @param uri     URL to the ownCloud server; BASE ENTRY POINT, not WebDavPATH
     * @param context Android context where the OwnCloudClient is being created.
     * @return A OwnCloudClient object ready to be used
     */
    public static NextcloudClient createNextcloudClient(Uri uri,
                                                        String userId,
                                                        String credentials,
                                                        Context context,
                                                        boolean followRedirects,
                                                        Interceptor interceptor) {
        try {
            NetworkUtils.registerAdvancedSslContext(true, context);
        } catch (GeneralSecurityException e) {
            Log_OC.e(TAG, "Advanced SSL Context could not be loaded. Default SSL management in" +
                    " the system will be used for HTTPS connections", e);

        } catch (IOException e) {
            Log_OC.e(TAG, "The local server truststore could not be read. Default SSL management" +
                    " in the system will be used for HTTPS connections", e);
        }

        NextcloudClient client;

        if (interceptor == null) {
            client = new NextcloudClient(uri, userId, credentials, context);
        } else {
            client = new NextcloudClient(uri, userId, credentials, interceptor, context);
        }
        client.setFollowRedirects(followRedirects);

        return client;
    }

    /**
     * Creates a NextcloudClient
     *
     * Do not call this method from the main thread.
     *
     * @param account                       The nextcloud account
     * @param appContext                    Android application context
     * @param interceptor
     * @return                              A Nextcloud object ready to be used
     * @throws AuthenticatorException       If the authenticator failed to get the authorization
     *                                      token for the account.
     * @throws OperationCanceledException   If the authenticator operation was cancelled while
     *                                      getting the authorization token for the account.
     * @throws IOException                  If there was some I/O error while getting the
     *                                      authorization token for the account.
     * @throws AccountNotFoundException     If 'account' is unknown for the AccountManager
     */
    public static NextcloudClient createNextcloudClient(Account account, Context appContext, Interceptor interceptor)
            throws OperationCanceledException, AuthenticatorException, IOException,
            AccountNotFoundException {
        //Log_OC.d(TAG, "Creating OwnCloudClient associated to " + account.name);
        Uri baseUri = Uri.parse(AccountUtils.getBaseUrlForAccount(appContext, account));
        AccountManager am = AccountManager.get(appContext);
        // TODO avoid calling to getUserData here
        String userId = am.getUserData(account, AccountUtils.Constants.KEY_USER_ID);


        String username = AccountUtils.getUsernameForAccount(account);
        String password = am.blockingGetAuthToken(account, AccountTypeUtils.getAuthTokenTypePass(account.type), false);

        // Restore cookies
        // TODO v2 cookie handling
        // AccountUtils.restoreCookies(account, client, appContext);

        return createNextcloudClient(baseUri,
                userId, Credentials.basic(username, password),
                appContext,
                true,
                interceptor);
    }
}
