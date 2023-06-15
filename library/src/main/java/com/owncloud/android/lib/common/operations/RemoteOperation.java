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

package com.owncloud.android.lib.common.operations;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import com.nextcloud.common.NextcloudClient;
import com.nextcloud.common.User;
import com.owncloud.android.lib.common.OwnCloudClient;

import kotlin.NotImplementedError;

/**
 * Operation which execution involves one or several interactions with an ownCloud server.
 * <p>
 * Provides methods to execute the operation both synchronously or asynchronously.
 *
 * @author David A. Velasco
 */
public abstract class RemoteOperation<T> implements Runnable {

    private static final String TAG = RemoteOperation.class.getSimpleName();

    /**
     * OCS API header name
     */
    public static final String OCS_API_HEADER = "OCS-APIREQUEST";
    public static final String OCS_ETAG_HEADER = "If-None-Match";

    /**
     * OCS API header value
     */
    public static final String OCS_API_HEADER_VALUE = "true";

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String JSON_ENCODED = "application/json";
    protected static final String E2E_TOKEN = "e2e-token";
    protected static final String REMOTE_WIPE_TOKEN = "token";
    protected static final String JSON_FORMAT = "?format=json";

    /** ownCloud account in the remote ownCloud server to operate */
    protected Account mAccount = null;
    
    /** Android Application context */
    protected Context mContext = null;
    
	/** Object to interact with the remote server */
	protected OwnCloudClient mClient = null;
    protected NextcloudClient clientNew = null;
	
	/** Callback object to notify about the execution of the remote operation */
	protected OnRemoteOperationListener mListener = null;
	
	/** Handler to the thread where mListener methods will be called */
	protected Handler mListenerHandler = null;

	/** Activity */
    protected Activity mCallerActivity;


   

  

    
    

    /**
     * Asynchronously executes the remote operation
     * 
     * This method should be used whenever an ownCloud account is available, instead of
     * {@link LegacyRemoteOperation#execute(OwnCloudClient)}.
     * 
     * @deprecated 	This method will be removed in version 1.0.
     *  			Use {@link #execute(Account, Context, OnRemoteOperationListener,
     *  			Handler)} instead.
     * 
     * @param account           ownCloud account in remote ownCloud server to reach during
     *                          the execution of the operation.
     * @param context           Android context for the component calling the method.
     * @param listener          Listener to be notified about the execution of the operation.
     * @param listenerHandler   Handler associated to the thread where the methods of the listener
     *                          objects must be called.
     * @return                  Thread were the remote operation is executed.
     */
	@Deprecated
    public Thread execute(Account account, Context context, OnRemoteOperationListener listener,
                          Handler listenerHandler, Activity callerActivity) {
        if (account == null) {
            throw new IllegalArgumentException("Trying to execute a remote operation with a NULL Account");
        }
        if (context == null) {
            throw new IllegalArgumentException("Trying to execute a remote operation with a NULL Context");
        }
        mAccount = account;
        mContext = context.getApplicationContext();
        mCallerActivity = callerActivity;
        mClient = null;     // client instance will be created from mAccount and mContext in the runnerThread below
        mListener = listener;
        
        mListenerHandler = listenerHandler;
        
        Thread runnerThread = new Thread(this);
        runnerThread.start();
        return runnerThread;
    }

    /**
     * This is a transitional wrapper around {@link #execute(Account, Context, OnRemoteOperationListener, Handler, Activity)}
     * using modern {@link User} interface instead of platform {@link Account}
     */
    @Deprecated
    public Thread execute(User user, Context context, OnRemoteOperationListener listener,
                          Handler listenerHandler, Activity callerActivity) {
	    return execute(user.toPlatformAccount(), context, listener, listenerHandler, callerActivity);
    }
    
    /**
     * Asynchronously executes the remote operation
     * 
     * This method should be used whenever an ownCloud account is available, 
     * instead of {@link #execute(OwnCloudClient, OnRemoteOperationListener, Handler))}.
     * 
     * @param account           ownCloud account in remote ownCloud server to reach during the 
     * 							execution of the operation.
     * @param context           Android context for the component calling the method.
     * @param listener          Listener to be notified about the execution of the operation.
     * @param listenerHandler   Handler associated to the thread where the methods of the listener 
     * 							objects must be called.
     * @return                  Thread were the remote operation is executed.
     */
    public Thread execute(Account account, Context context,
                          OnRemoteOperationListener listener, Handler listenerHandler) {

        if (account == null) {
            throw new IllegalArgumentException("Trying to execute a remote operation with a NULL Account");
        }
        if (context == null) {
            throw new IllegalArgumentException("Trying to execute a remote operation with a NULL Context");
        }
        mAccount = account;
        mContext = context.getApplicationContext();
        mCallerActivity = null;
        mClient = null;     // the client instance will be created from
                            // mAccount and mContext in the runnerThread to create below
        
        mListener = listener;
        
        mListenerHandler = listenerHandler;
        
        Thread runnerThread = new Thread(this);
        runnerThread.start();
        return runnerThread;
    }

    /**
     * This is a transitional wrapper around
     * {@link #execute(Account, Context, OnRemoteOperationListener, Handler)}
     * using modern {@link User} interface instead of platform {@link Account}
     */
    public Thread execute(User user, Context context,
                          OnRemoteOperationListener listener, Handler listenerHandler) {
        return execute(user.toPlatformAccount(), context, listener, listenerHandler);
    }
    
	/**
	 * Asynchronously executes the remote operation
	 * 
	 * @param client			Client object to reach an ownCloud server
     *                          during the execution of the operation.
	 * @param listener			Listener to be notified about the execution of the operation.
	 * @param listenerHandler	Handler associated to the thread where the methods of
     *                          the listener objects must be called.
	 * @return					Thread were the remote operation is executed.
	 */
    public Thread execute(OwnCloudClient client, OnRemoteOperationListener listener, Handler listenerHandler) {
		if (client == null) {
            throw new IllegalArgumentException("Trying to execute a remote operation with a NULL OwnCloudClient");
		}
		mClient = client;
		
		if (listener == null) {
            throw new IllegalArgumentException("Trying to execute a remote operation asynchronously " +
                                                       "without a listener to notify the result");
		}
		mListener = listener;
		
		if (listenerHandler == null) {
            throw new IllegalArgumentException("Trying to execute a remote operation asynchronously " +
                                                       "without a handler to the listener's thread");
		}
		mListenerHandler = listenerHandler;
		
		Thread runnerThread = new Thread(this);
		runnerThread.start();
		return runnerThread;
	}
	



    /**
     * Returns the current client instance to access the remote server.
     * 
     * @return      Current client instance to access the remote server.
     */
    public final OwnCloudClient getClient() {
        return mClient;
    }

    @Override
    public void run() {
        throw new NotImplementedError("this should never be used!");
    }
}
