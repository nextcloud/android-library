/* ownCloud Android Library is available under MIT license
 *   Copyright (C) 2014 ownCloud Inc.
 *   Copyright (C) 2012  Bartek Przybylski
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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.HttpStatus;
import org.apache.http.params.CoreProtocolPNames;

import com.owncloud.android.lib.common.network.WebdavUtils;


import android.net.Uri;
import android.util.Log;

public class OwnCloudClient extends HttpClient {
	
    private static final String TAG = OwnCloudClient.class.getSimpleName();
    public static final String USER_AGENT = "Android-ownCloud";
    private static final int MAX_REDIRECTIONS_COUNT = 3;
    private static final String PARAM_SINGLE_COOKIE_HEADER = "http.protocol.single-cookie-header";
    private static final boolean PARAM_SINGLE_COOKIE_HEADER_VALUE = true;
    
    private static byte[] sExhaustBuffer = new byte[1024];
    
    private static int sIntanceCounter = 0;
    private boolean mFollowRedirects = true;
    //private Credentials mCredentials = null;
    private OwnCloudCredentials mCredentials = null;
    //private String mSsoSessionCookie = null;
    private int mInstanceNumber = 0;
    
    private Uri mUri;
    private Uri mWebdavUri;
    
    /**
     * Constructor
     */
    public OwnCloudClient(HttpConnectionManager connectionMgr) {
        super(connectionMgr);
        
        mInstanceNumber = sIntanceCounter++;
        Log.d(TAG + " #" + mInstanceNumber, "Creating OwnCloudClient");
        
        getParams().setParameter(HttpMethodParams.USER_AGENT, USER_AGENT);
        getParams().setParameter(
        		CoreProtocolPNames.PROTOCOL_VERSION, 
        		HttpVersion.HTTP_1_1);
        
        getParams().setCookiePolicy(
        		CookiePolicy.BROWSER_COMPATIBILITY);	// to keep sessions
        getParams().setParameter(
        		PARAM_SINGLE_COOKIE_HEADER, 			// to avoid problems with some web servers
        		PARAM_SINGLE_COOKIE_HEADER_VALUE);
    }

    
    public void setCredentials(OwnCloudCredentials credentials) {
    	if (credentials != null) {
        	mCredentials = credentials;
    		mCredentials.applyTo(this);
    	} else {
    		clearCredentials();
    	}
    }
    
    /*
    public void setBearerCredentials(String accessToken) {
        AuthPolicy.registerAuthScheme(BearerAuthScheme.AUTH_POLICY, BearerAuthScheme.class);
        
        List<String> authPrefs = new ArrayList<String>(1);
        authPrefs.add(BearerAuthScheme.AUTH_POLICY);
        getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);        
        
        getParams().setAuthenticationPreemptive(true);
        mCredentials = new BearerCredentials(accessToken);
        getState().setCredentials(AuthScope.ANY, mCredentials);
        mSsoSessionCookie = null;
    }
    */

    /*
    public void setBasicCredentials(String username, String password) {
        List<String> authPrefs = new ArrayList<String>(1);
        authPrefs.add(AuthPolicy.BASIC);
        getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);        
        
        getParams().setAuthenticationPreemptive(true);
        mCredentials = new UsernamePasswordCredentials(username, password);
        getState().setCredentials(AuthScope.ANY, mCredentials);
        mSsoSessionCookie = null;
    }
    */
    
    /*
    public void setSsoSessionCookie(String accessToken) {
        Log.d(TAG + " #" + mInstanceNumber, "Setting session cookie: " + accessToken);
        Log.e(TAG + " #" + mInstanceNumber, "BASE URL: " + mUri);
        Log.e(TAG + " #" + mInstanceNumber, "WebDAV URL: " + mWebdavUri);
        
        if (accessToken != null && accessToken.length() > 0) {
        
	        getParams().setAuthenticationPreemptive(false);
	        
	        mSsoSessionCookie = accessToken;
	        mCredentials = null;
	
	        Uri serverUri = (mUri != null)? mUri : mWebdavUri;
	        	// TODO refactoring the mess of URIs
	        
	        String[] cookies = mSsoSessionCookie.split(";");
	        if (cookies.length > 0) {
	        	//Cookie[] cookies = new Cookie[cookiesStr.length];
	            for (int i=0; i<cookies.length; i++) {
	            	Cookie cookie = new Cookie();
	            	int equalPos = cookies[i].indexOf('=');
	            	cookie.setName(cookies[i].substring(0, equalPos));
	            	//Log.d(TAG, "Set name for cookie: " + cookies[i].substring(0, equalPos));
	    	        cookie.setValue(cookies[i].substring(equalPos + 1));
	            	//Log.d(TAG, "Set value for cookie: " + cookies[i].substring(equalPos + 1));
	    	        cookie.setDomain(serverUri.getHost());	// VERY IMPORTANT 
	            	//Log.d(TAG, "Set domain for cookie: " + serverUri.getHost());
	    	        cookie.setPath(serverUri.getPath());	// VERY IMPORTANT
	            	Log.d(TAG, "Set path for cookie: " + serverUri.getPath());
	    	        getState().addCookie(cookie);
	            }
	        }
	        
        } else {
        	Log.e(TAG, "Setting access token " + accessToken);
        }
    }
    */
    
    public void clearCredentials() {
        mCredentials = null;
        getState().clearCredentials();
        getState().clearCookies();
    }
    
    /**
     * Check if a file exists in the OC server
     * 
     * TODO replace with ExistenceOperation
     * 
     * @return              'true' if the file exists; 'false' it doesn't exist
     * @throws  Exception   When the existence could not be determined
     */
    public boolean existsFile(String path) throws IOException, HttpException {
        HeadMethod head = new HeadMethod(mWebdavUri.toString() + WebdavUtils.encodePath(path));
        try {
            int status = executeMethod(head);
            Log.d(TAG, "HEAD to " + path + " finished with HTTP status " + status +
            		((status != HttpStatus.SC_OK)?"(FAIL)":""));
            exhaustResponse(head.getResponseBodyAsStream());
            return (status == HttpStatus.SC_OK);
            
        } finally {
            head.releaseConnection();    // let the connection available for other methods
        }
    }
    
    /**
     * Requests the received method with the received timeout (milliseconds).
     * 
     * Executes the method through the inherited HttpClient.executedMethod(method).
     * 
     * Sets the socket and connection timeouts only for the method received.
     * 
     * The timeouts are both in milliseconds; 0 means 'infinite'; 
     * < 0 means 'do not change the default'
     * 
     * @param method            HTTP method request.
     * @param readTimeout       Timeout to set for data reception
     * @param conntionTimout    Timeout to set for connection establishment
     */
    public int executeMethod(HttpMethodBase method, int readTimeout, int connectionTimeout) 
    		throws HttpException, IOException {
        int oldSoTimeout = getParams().getSoTimeout();
        int oldConnectionTimeout = getHttpConnectionManager().getParams().getConnectionTimeout();
        try {
            if (readTimeout >= 0) { 
                method.getParams().setSoTimeout(readTimeout);   // this should be enough...
                getParams().setSoTimeout(readTimeout);          // ... but HTTPS needs this
            }
            if (connectionTimeout >= 0) {
                getHttpConnectionManager().getParams().setConnectionTimeout(connectionTimeout);
            }
            return executeMethod(method);
        } finally {
            getParams().setSoTimeout(oldSoTimeout);
            getHttpConnectionManager().getParams().setConnectionTimeout(oldConnectionTimeout);
        }
    }
    
    
    @Override
    public int executeMethod(HttpMethod method) throws IOException, HttpException {
        try {	// just to log 
	        boolean customRedirectionNeeded = false;
	        try {
	            method.setFollowRedirects(mFollowRedirects);
	        } catch (Exception e) {
	        	/*
	            if (mFollowRedirects) 
	        		Log_OC.d(TAG, "setFollowRedirects failed for " + method.getName() 
	        			+ " method, custom redirection will be used if needed");
        		*/
	            customRedirectionNeeded = mFollowRedirects;
	        }
        
	        Log.d(TAG + " #" + mInstanceNumber, "REQUEST " + 
	        		method.getName() + " " + method.getPath());
        
	        logCookiesAtRequest(method.getRequestHeaders(), "before");
	        logCookiesAtState("before");
	        
	        int status = super.executeMethod(method);
        
	        if (customRedirectionNeeded) {
	        	status = patchRedirection(status, method);
	        }

	        logCookiesAtRequest(method.getRequestHeaders(), "after");
	        logCookiesAtState("after");
	        logSetCookiesAtResponse(method.getResponseHeaders());
	        
	        return status;
	        
        } catch (IOException e) {
        	Log.d(TAG + " #" + mInstanceNumber, "Exception occured", e);
        	throw e;
        }
    }

	private int patchRedirection(int status, HttpMethod method) throws HttpException, IOException {
        int redirectionsCount = 0;
        while (redirectionsCount < MAX_REDIRECTIONS_COUNT &&
                (   status == HttpStatus.SC_MOVED_PERMANENTLY || 
                    status == HttpStatus.SC_MOVED_TEMPORARILY ||
                    status == HttpStatus.SC_TEMPORARY_REDIRECT)
                ) {
            
            Header location = method.getResponseHeader("Location");
            if (location == null) {
            	location = method.getResponseHeader("location");
            }
            if (location != null) {
                Log.d(TAG + " #" + mInstanceNumber,  
                		"Location to redirect: " + location.getValue());
                method.setURI(new URI(location.getValue(), true));
                status = super.executeMethod(method);
                redirectionsCount++;
                
            } else {
                Log.d(TAG + " #" + mInstanceNumber,  "No location to redirect!");
                status = HttpStatus.SC_NOT_FOUND;
            }
        }
        return status;
	}

	/**
     * Exhausts a not interesting HTTP response. Encouraged by HttpClient documentation.
     * 
     * @param responseBodyAsStream      InputStream with the HTTP response to exhaust.
     */
    public void exhaustResponse(InputStream responseBodyAsStream) {
        if (responseBodyAsStream != null) {
            try {
                while (responseBodyAsStream.read(sExhaustBuffer) >= 0);
                responseBodyAsStream.close();
            
            } catch (IOException io) {
                Log.e(TAG, "Unexpected exception while exhausting not interesting HTTP response;" +
                		" will be IGNORED", io);
            }
        }
    }

    /**
     * Sets the connection and wait-for-data timeouts to be applied by default to the methods 
     * performed by this client.
     */
    public void setDefaultTimeouts(int defaultDataTimeout, int defaultConnectionTimeout) {
            getParams().setSoTimeout(defaultDataTimeout);
            getHttpConnectionManager().getParams().setConnectionTimeout(defaultConnectionTimeout);
    }

    /**
     * Sets the Webdav URI for the helper methods that receive paths as parameters, 
     * instead of full URLs
     * @param uri
     */
    public void setWebdavUri(Uri uri) {
        mWebdavUri = uri;
    }

    public Uri getWebdavUri() {
        return mWebdavUri;
    }
    
    /**
     * Sets the base URI for the helper methods that receive paths as parameters, 
     * instead of full URLs
     * 
     * @param uri
     */
    public void setBaseUri(Uri uri) {
        mUri = uri;
    }

    public Uri getBaseUri() {
        return mUri;
    }

    /*
    public final Credentials getCredentials() {
        return mCredentials;
    }
    */
    
    public final OwnCloudCredentials getCredentials() {
        return mCredentials;
    }
    
    /*
    public final String getSsoSessionCookie() {
        return mSsoSessionCookie;
    }
    */

    public void setFollowRedirects(boolean followRedirects) {
        mFollowRedirects = followRedirects;
    }

    
	private void logCookiesAtRequest(Header[] headers, String when) {
        int counter = 0;
        for (int i=0; i<headers.length; i++) {
        	if (headers[i].getName().toLowerCase().equals("cookie")) {
        		Log.d(TAG + " #" + mInstanceNumber, 
        				"Cookies at request (" + when + ") (" + counter++ + "): " + 
        						headers[i].getValue());
        	}
        }
        if (counter == 0) {
    		Log.d(TAG + " #" + mInstanceNumber, "No cookie at request before");
        }
	}

    private void logCookiesAtState(String string) {
        Cookie[] cookies = getState().getCookies();
        if (cookies.length == 0) {
    		Log.d(TAG + " #" + mInstanceNumber, "No cookie at STATE before");
        } else {
    		Log.d(TAG + " #" + mInstanceNumber, "Cookies at STATE (before)");
	        for (int i=0; i<cookies.length; i++) {
	    		Log.d(TAG + " #" + mInstanceNumber, "    (" + i + "):" +
	    				"\n        name: " + cookies[i].getName() +
	    				"\n        value: " + cookies[i].getValue() +
	    				"\n        domain: " + cookies[i].getDomain() +
	    				"\n        path: " + cookies[i].getPath()
	    				);
	        }
        }
	}

	private void logSetCookiesAtResponse(Header[] headers) {
        int counter = 0;
        for (int i=0; i<headers.length; i++) {
        	if (headers[i].getName().toLowerCase().equals("set-cookie")) {
        		Log.d(TAG + " #" + mInstanceNumber, 
        				"Set-Cookie (" + counter++ + "): " + headers[i].getValue());
        	}
        }
        if (counter == 0) {
    		Log.d(TAG + " #" + mInstanceNumber, "No set-cookie");
        }
        
	}
	
	public String getCookiesString(){
		Cookie[] cookies = getState().getCookies(); 
		String cookiesString ="";
		for (Cookie cookie: cookies) {
			cookiesString = cookiesString + cookie.toString() + ";";
			
			logCookie(cookie);
		}
		
		return cookiesString;
		
	}
	
	private void logCookie(Cookie cookie) {
    	Log.d(TAG, "Cookie name: "+ cookie.getName() );
    	Log.d(TAG, "       value: "+ cookie.getValue() );
    	Log.d(TAG, "       domain: "+ cookie.getDomain());
    	Log.d(TAG, "       path: "+ cookie.getPath() );
    	Log.d(TAG, "       version: "+ cookie.getVersion() );
    	Log.d(TAG, "       expiryDate: " + 
    			(cookie.getExpiryDate() != null ? cookie.getExpiryDate().toString() : "--"));
    	Log.d(TAG, "       comment: "+ cookie.getComment() );
    	Log.d(TAG, "       secure: "+ cookie.getSecure() );
    }


}
