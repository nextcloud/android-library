/* ownCloud Android Library is available under MIT license
 *   Copyright (C) 2015 ownCloud Inc.
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

import android.content.Context;
import android.net.Uri;

import com.nextcloud.common.DNSCache;
import com.nextcloud.common.NextcloudUriDelegate;
import com.owncloud.android.lib.common.accounts.AccountUtils;
import com.owncloud.android.lib.common.network.RedirectionPath;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Locale;

import de.ritscher.ssl.InteractiveKeyManager;
import lombok.Getter;
import lombok.Setter;

public class OwnCloudClient extends HttpClient {

    private static final String TAG = OwnCloudClient.class.getSimpleName();
    public static final int MAX_REDIRECTIONS_COUNT = 3;
    private static final String PARAM_SINGLE_COOKIE_HEADER = "http.protocol.single-cookie-header";
    private static final boolean PARAM_SINGLE_COOKIE_HEADER_VALUE = true;
    private static final String PARAM_PROTOCOL_VERSION = "http.protocol.version";

    private static byte[] sExhaustBuffer = new byte[1024];

    private static int sInstanceCounter = 0;
    private final NextcloudUriDelegate nextcloudUriDelegate;
    private boolean followRedirects = true;
    private OwnCloudCredentials credentials = null;
    private int mInstanceNumber;

    @Getter private Uri baseUri;
    @Setter private String userId;
    private Context context;

    /**
     * Constructor
     */
    public OwnCloudClient(Uri baseUri, HttpConnectionManager connectionMgr, Context context) {
        super(connectionMgr);

        if (baseUri == null) {
        	throw new IllegalArgumentException("Parameter 'baseUri' cannot be NULL");
        }
        nextcloudUriDelegate = new NextcloudUriDelegate(baseUri);

        this.baseUri = baseUri;
        this.context = context;
        
        mInstanceNumber = sInstanceCounter++;
        Log_OC.d(TAG + " #" + mInstanceNumber, "Creating OwnCloudClient");

        String userAgent;

        userAgent = OwnCloudClientManagerFactory.getUserAgent();

        getParams().setParameter(HttpMethodParams.USER_AGENT, userAgent);
        getParams().setParameter(PARAM_PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        // to avoid problems with some web servers
        getParams().setParameter(PARAM_SINGLE_COOKIE_HEADER, PARAM_SINGLE_COOKIE_HEADER_VALUE);

        applyProxySettings();

        clearCredentials();
    }


    private void applyProxySettings() {
    	String proxyHost = System.getProperty("http.proxyHost");
    	String proxyPortSt = System.getProperty("http.proxyPort");
    	int proxyPort = 0;
    	try {
    		if (proxyPortSt != null && proxyPortSt.length() > 0) {
    			proxyPort = Integer.parseInt(proxyPortSt);
    		}
    	} catch (Exception e) {
    		// nothing to do here
    	}

    	if (proxyHost != null && proxyHost.length() > 0) {
	    	HostConfiguration hostCfg = getHostConfiguration();
	    	hostCfg.setProxy(proxyHost, proxyPort);
	    	Log_OC.d(TAG, "Proxy settings: " + proxyHost + ":" + proxyPort);
    	}
	}


	public void setCredentials(OwnCloudCredentials credentials) {
    	if (credentials != null) {
    		this.credentials = credentials;
            this.credentials.applyTo(this);
    	} else {
    		clearCredentials();
    	}
    }

    public void clearCredentials() {
		if (!(credentials instanceof OwnCloudAnonymousCredentials)) {
			credentials = OwnCloudCredentialsFactory.getAnonymousCredentials();
		}
		credentials.applyTo(this);
	}

    /**
     * Requests the received method with the received timeout (milliseconds).
     * <p>
     * Executes the method through the inherited HttpClient.executedMethod(method).
     * <p>
     * Sets the socket and connection timeouts only for the method received.
     * <p>
     * The timeouts are both in milliseconds; 0 means 'infinite';
     * < 0 means 'do not change the default'
     *
     * @param method            HTTP method request.
     * @param readTimeout       Timeout to set for data reception
     * @param connectionTimeout Timeout to set for connection establishment
     */
    public int executeMethod(HttpMethodBase method, int readTimeout, int connectionTimeout) throws IOException {

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


    /**
     * Requests the received method.
     *
     * Executes the method through the inherited HttpClient.executedMethod(method).
     *
     * @param method                HTTP method request.
     */
    @Override
    public int executeMethod(HttpMethod method) throws IOException {
        final String hostname = method.getURI().getHost();

        try {
            // Update User Agent
            HttpParams params = method.getParams();

            params.setParameter(HttpMethodParams.USER_AGENT, OwnCloudClientManagerFactory.getUserAgent());

            Log_OC.d(TAG + " #" + mInstanceNumber, "REQUEST " + method.getName() + " " + method.getPath());

//	        logCookiesAtRequest(method.getRequestHeaders(), "before");
//	        logCookiesAtState("before");
            method.setFollowRedirects(false);

            int status = super.executeMethod(method);

            if (status >= 500 && status < 600 && DNSCache.isIPV6First(hostname)) {
                return retryMethodWithIPv4(method, hostname);
            }

            if (followRedirects) {
                status = followRedirection(method).getLastStatus();
            }

//	        logCookiesAtRequest(method.getRequestHeaders(), "after");
//	        logCookiesAtState("after");
//	        logSetCookiesAtResponse(method.getResponseHeaders());

            if (status >= 400 && status < 500) {
                Log_OC.w(TAG, "executeMethod failed with error code " + status + "; remove key chain aliases disabled");
                //new InteractiveKeyManager(context).removeKeys(baseUri.getHost(), baseUri.getPort());
            }
            return status;

        } catch (SocketTimeoutException | ConnectException e) {
            if (DNSCache.isIPV6First(hostname)) {
                return retryMethodWithIPv4(method, hostname);
            } else {
                throw e;
            }
        } catch (IOException e) {
            //Log_OC.d(TAG + " #" + mInstanceNumber, "Exception occurred", e);
            throw e;
        }
    }

    private int retryMethodWithIPv4(HttpMethod method, String hostname) throws IOException {
        Log_OC.d(TAG, "IPv6 connection failed. Retrying with IPV4");
        DNSCache.setIPVersionPreference(hostname, true);
        return executeMethod(method);
    }


    public RedirectionPath followRedirection(HttpMethod method) throws IOException {
        int redirectionsCount = 0;
        int status = method.getStatusCode();
        RedirectionPath result = new RedirectionPath(status, MAX_REDIRECTIONS_COUNT);
        while (redirectionsCount < MAX_REDIRECTIONS_COUNT &&
                (status == HttpStatus.SC_MOVED_PERMANENTLY ||
                        status == HttpStatus.SC_MOVED_TEMPORARILY ||
                        status == HttpStatus.SC_TEMPORARY_REDIRECT)
                ) {

            Header location = method.getResponseHeader("Location");
            if (location == null) {
            	location = method.getResponseHeader("location");
            }
            if (location != null) {
                Log_OC.d(TAG + " #" + mInstanceNumber,
                        "Location to redirect: " + location.getValue());

                String locationStr = location.getValue();
                result.addLocation(locationStr);

                // Release the connection to avoid reach the max number of connections per host
                // due to it will be set a different url
                exhaustResponse(method.getResponseBodyAsStream());
                method.releaseConnection();

                method.setURI(new URI(locationStr, true));
                Header destination = method.getRequestHeader("Destination");
                if (destination == null) {
                	destination = method.getRequestHeader("destination");
                }
                if (destination != null) {
                    int suffixIndex = locationStr.lastIndexOf(AccountUtils.WEBDAV_PATH_9_0);
                    String redirectionBase = locationStr.substring(0, suffixIndex);

                    String destinationStr = destination.getValue();
                    String destinationPath = destinationStr.substring(getBaseUri().toString().length());
                    String redirectedDestination = redirectionBase + destinationPath;

                    destination.setValue(redirectedDestination);
                    method.setRequestHeader(destination);
                }
                status = super.executeMethod(method);
                result.addStatus(status);
                redirectionsCount++;

            } else {
                Log_OC.d(TAG + " #" + mInstanceNumber,  "No location to redirect!");
                status = HttpStatus.SC_NOT_FOUND;
            }
        }
        return result;
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
                Log_OC.e(TAG, "Unexpected exception while exhausting not interesting HTTP response;" +
                		" will be IGNORED", io);
            }
        }
    }

    /**
     * Sets the connection and wait-for-data timeouts to be applied by default to the methods
     * performed by this client.
     */
    public void setDefaultTimeouts(int defaultDataTimeout, int defaultConnectionTimeout) {
        if (defaultDataTimeout >= 0) {
            getParams().setSoTimeout(defaultDataTimeout);
        }
        if (defaultConnectionTimeout >= 0) {
            getHttpConnectionManager().getParams().setConnectionTimeout(defaultConnectionTimeout);
        }
    }

    public String getFilesDavUri(String path) {
        return nextcloudUriDelegate.getFilesDavUri(path);
    }

    public Uri getFilesDavUri() {
        return nextcloudUriDelegate.getFilesDavUri();
    }

    public Uri getUploadUri() {
        return nextcloudUriDelegate.getUploadUri();
    }

    public Uri getDavUri() {
        return nextcloudUriDelegate.getDavUri();
    }

    public String getCommentsUri(String fileId) {
        return nextcloudUriDelegate.getCommentsUri(fileId);
    }


    public Uri getBaseUri() {
        return nextcloudUriDelegate.getBaseUri();
    }

    public void setBaseUri(Uri uri) {
        nextcloudUriDelegate.setBaseUri(uri);
    }

    public void setUserId(String userId) {
        nextcloudUriDelegate.setUserId(userId);
    }

    private void logCookiesAtRequest(Header[] headers, String when) {
        int counter = 0;
        for (Header header : headers) {
            if ("cookie".equals(header.getName().toLowerCase(Locale.US))) {
                Log_OC.d(TAG + " #" + mInstanceNumber,
                         "Cookies at request (" + when + ") (" + counter++ + "): " +
                                 header.getValue());
            }
        }

        if (counter == 0) {
            Log_OC.d(TAG + " #" + mInstanceNumber, "No cookie at request (" + when + ")");
        }
	}

    private void logCookiesAtState(String when) {
        Cookie[] cookies = getState().getCookies();
        if (cookies.length == 0) {
            Log_OC.d(TAG + " #" + mInstanceNumber, "No cookie at STATE " + when);
        } else {
            Log_OC.d(TAG + " #" + mInstanceNumber, "Cookies at STATE (" + when + ")");
            for (int i=0; i<cookies.length; i++) {
	    		Log_OC.d(TAG + " #" + mInstanceNumber, "    (" + i + "):" +
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
        for (Header header : headers) {
            if ("set-cookie".equals(header.getName().toLowerCase(Locale.US))) {
                Log_OC.d(TAG + " #" + mInstanceNumber, "Set-Cookie (" + counter++ + "): " + header.getValue());
            }
        }
        if (counter == 0) {
            Log_OC.d(TAG + " #" + mInstanceNumber, "No set-cookie");
        }

    }

    public String getCookiesString() {
        Cookie[] cookies = getState().getCookies();
        String cookiesString = "";
        for (Cookie cookie : cookies) {
            cookiesString = cookiesString + cookie.toString() + ";";

            // logCookie(cookie);
        }

        return cookiesString;

    }

    public int getConnectionTimeout() {
        return getHttpConnectionManager().getParams().getConnectionTimeout();
    }

    public int getDataTimeout() {
        return getParams().getSoTimeout();
    }

    private void logCookie(Cookie cookie) {
        Log_OC.d(TAG, "Cookie name: " + cookie.getName());
        Log_OC.d(TAG, "       value: " + cookie.getValue());
        Log_OC.d(TAG, "       domain: " + cookie.getDomain());
        Log_OC.d(TAG, "       path: " + cookie.getPath());
        Log_OC.d(TAG, "       version: " + cookie.getVersion());
        Log_OC.d(TAG, "       expiryDate: " +
                (cookie.getExpiryDate() != null ? cookie.getExpiryDate().toString() : "--"));
        Log_OC.d(TAG, "       comment: " + cookie.getComment());
        Log_OC.d(TAG, "       secure: " + cookie.getSecure());
    }

    /**
     * Returns internally, never changing id of user
     *
     * @return uri-encoded userId
     */
    public String getUserId() {
        return nextcloudUriDelegate.getUserIdEncoded();
    }

    public String getUserIdPlain() {
        return nextcloudUriDelegate.getUserId();
    }

    public boolean isFollowRedirects() {
        return this.followRedirects;
    }

    public OwnCloudCredentials getCredentials() {
        return this.credentials;
    }

    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }
}
