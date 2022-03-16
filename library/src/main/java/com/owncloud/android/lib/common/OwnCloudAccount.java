/*   ownCloud Android Library is available under MIT license
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
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.owncloud.android.lib.common.accounts.AccountUtils;
import com.owncloud.android.lib.common.accounts.AccountUtils.AccountNotFoundException;

import java.io.IOException;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * OwnCloud Account
 * 
 * @author David A. Velasco
 */
@EqualsAndHashCode
public class OwnCloudAccount implements Parcelable {

    @Getter private Uri baseUri;
    @Getter private OwnCloudCredentials credentials;

    private String displayName;

    @Getter private String name;
    @Getter private Account savedAccount;

    /**
     * Constructor for already saved OC accounts.
     *
     * Do not use for anonymous credentials.
     */
    public OwnCloudAccount(Account savedAccount, Context context) throws AccountNotFoundException {
        if (savedAccount == null) {
            throw new IllegalArgumentException("Parameter 'savedAccount' cannot be null");
        }

        if (context == null) {
            throw new IllegalArgumentException("Parameter 'context' cannot be null");
        }

        this.savedAccount = savedAccount;
        name = savedAccount.name;
        credentials = null;    // load of credentials is delayed

        AccountManager ama = AccountManager.get(context.getApplicationContext());
        String baseUrl = ama.getUserData(this.savedAccount, AccountUtils.Constants.KEY_OC_BASE_URL);
        if (baseUrl == null ) {
            throw new AccountNotFoundException(this.savedAccount, "Account not found", null);
        }
        baseUri = Uri.parse(AccountUtils.getBaseUrlForAccount(context, this.savedAccount));
        displayName = ama.getUserData(this.savedAccount, AccountUtils.Constants.KEY_DISPLAY_NAME);
    }


    /**
     * Constructor for non yet saved OC accounts.
     *
     * @param baseUri           URI to the OC server to get access to.
     * @param credentials       Credentials to authenticate in the server. NULL is valid for anonymous credentials.
     */
    public OwnCloudAccount(Uri baseUri, OwnCloudCredentials credentials) {
        if (baseUri == null) {
            throw new IllegalArgumentException("Parameter 'baseUri' cannot be null");
        }
        savedAccount = null;
        name = null;
        this.baseUri = baseUri;
        this.credentials = credentials != null ? credentials : OwnCloudCredentialsFactory.getAnonymousCredentials();
        String username = this.credentials.getUsername();
        if (username != null) {
            name = AccountUtils.buildAccountName(this.baseUri, username);
        }
    }


    /**
     * Method for deferred load of account attributes from AccountManager
     *
     * @param context
     * @throws AuthenticatorException
     * @throws IOException
     * @throws OperationCanceledException
     */
    public void loadCredentials(Context context)
            throws AuthenticatorException,
                IOException, OperationCanceledException {

        if (context == null) {
            throw new IllegalArgumentException("Parameter 'context' cannot be null");
        }

		if (savedAccount != null) {
			credentials = AccountUtils.getCredentialsForAccount(context, savedAccount);
		}
	}

    public String getDisplayName() {
        if (displayName != null && displayName.length() > 0) {
            return displayName;
        } else if (credentials != null) {
            return credentials.getUsername();
        } else if (savedAccount != null) {
            return AccountUtils.getUsernameForAccount(savedAccount);
        } else {
            return null;
        }
    }

    /*
     * Autogenerated Parcelable interface
     */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(baseUri, flags);
        dest.writeParcelable(credentials, flags);
        dest.writeString(displayName);
        dest.writeString(name);
        dest.writeParcelable(savedAccount, flags);
    }

    protected OwnCloudAccount(Parcel in) {
        baseUri = in.readParcelable(Uri.class.getClassLoader());
        credentials = in.readParcelable(OwnCloudCredentials.class.getClassLoader());
        displayName = in.readString();
        name = in.readString();
        savedAccount = in.readParcelable(Account.class.getClassLoader());
    }

    public static final Creator<OwnCloudAccount> CREATOR = new Creator<OwnCloudAccount>() {
        @Override
        public OwnCloudAccount createFromParcel(Parcel source) {
            return new OwnCloudAccount(source);
        }

        @Override
        public OwnCloudAccount[] newArray(int size) {
            return new OwnCloudAccount[size];
        }
    };
}
