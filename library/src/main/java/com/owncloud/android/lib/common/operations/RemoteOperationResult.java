/* ownCloud Android Library is available under MIT license
 *   Copyright (C) 2016 ownCloud GmbH.
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
import android.accounts.AccountsException;
import android.os.Build;
import android.system.ErrnoException;
import android.system.OsConstants;

import com.nextcloud.common.OkHttpMethodBase;
import com.owncloud.android.lib.common.accounts.AccountUtils.AccountNotFoundException;
import com.owncloud.android.lib.common.network.CertificateCombinedException;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.notifications.models.Notification;
import com.owncloud.android.lib.resources.notifications.models.PushResponse;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.jackrabbit.webdav.DavException;
import org.json.JSONException;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.SSLException;

import okhttp3.Headers;


/**
 * The result of a remote operation required to an ownCloud server.
 * <p>
 * Provides a common classification of remote operation results for all the application.
 *
 * @author David A. Velasco
 */
public class RemoteOperationResult<T extends Object> implements Serializable {

    // Generated - should be refreshed every time the class changes!!
    private static final long serialVersionUID = -4325446958558896222L;
    private static final String TAG = RemoteOperationResult.class.getSimpleName();
    private static final String HEADER_WWW_AUTHENTICATE = "www-authenticate";
    private static final String HEADER_LOCATION = "location";

    public enum ResultCode {
        OK,
        OK_SSL,
        OK_NO_SSL,
        UNHANDLED_HTTP_CODE,
        UNAUTHORIZED,
        FILE_NOT_FOUND,
        INSTANCE_NOT_CONFIGURED,
        UNKNOWN_ERROR,
        WRONG_CONNECTION,
        TIMEOUT,
        INCORRECT_ADDRESS,
        HOST_NOT_AVAILABLE,
        NO_NETWORK_CONNECTION,
        SSL_ERROR,
        SSL_RECOVERABLE_PEER_UNVERIFIED,
        BAD_OC_VERSION,
        CANCELLED,
        INVALID_LOCAL_FILE_NAME,
        INVALID_OVERWRITE,
        CONFLICT,
        OAUTH2_ERROR,
        SYNC_CONFLICT,
        LOCAL_STORAGE_FULL,
        LOCAL_STORAGE_NOT_MOVED,
        LOCAL_STORAGE_NOT_COPIED,
        OAUTH2_ERROR_ACCESS_DENIED,
        QUOTA_EXCEEDED,
        ACCOUNT_NOT_FOUND,
        ACCOUNT_EXCEPTION,
        ACCOUNT_NOT_NEW,
        ACCOUNT_NOT_THE_SAME,
        INVALID_CHARACTER_IN_NAME,
        SHARE_NOT_FOUND,
        LOCAL_STORAGE_NOT_REMOVED,
        FORBIDDEN,
        SHARE_FORBIDDEN,
        OK_REDIRECT_TO_NON_SECURE_CONNECTION, 
        INVALID_MOVE_INTO_DESCENDANT,
        INVALID_COPY_INTO_DESCENDANT,
        PARTIAL_MOVE_DONE,
        PARTIAL_COPY_DONE,
        SHARE_WRONG_PARAMETER,
        WRONG_SERVER_RESPONSE,
        INVALID_CHARACTER_DETECT_IN_SERVER,
        DELAYED_FOR_WIFI,
        DELAYED_FOR_CHARGING,
        LOCAL_FILE_NOT_FOUND,
        NOT_AVAILABLE,
        MAINTENANCE_MODE,
        LOCK_FAILED,
        DELAYED_IN_POWER_SAVE_MODE,
        ACCOUNT_USES_STANDARD_PASSWORD,
        METADATA_NOT_FOUND,
        OLD_ANDROID_API,
        UNTRUSTED_DOMAIN,
        ETAG_CHANGED,
        ETAG_UNCHANGED,
        VIRUS_DETECTED,
        FOLDER_ALREADY_EXISTS,
        CANNOT_CREATE_FILE
    }

    private boolean mSuccess = false;
    private int mHttpCode = -1;
    private String mHttpPhrase = null;
    private Exception mException = null;
    private ResultCode mCode = ResultCode.UNKNOWN_ERROR;
    private String message;
    private String mRedirectedLocation;
    private ArrayList<String> mAuthenticateHeaders = new ArrayList<>();
    private String mLastPermanentLocation = null;

    private ArrayList<Object> mData;
    private T resultData;
    private List<Notification> mNotificationData;
    private PushResponse mPushResponse;

    /**
     * Public constructor from result code.
     * <p>
     * To be used when the caller takes the responsibility of interpreting the result of a {@link RemoteOperation}
     *
     * @param code {@link ResultCode} decided by the caller.
     */
    public RemoteOperationResult(ResultCode code) {
        mCode = code;
        mSuccess = (code == ResultCode.OK || code == ResultCode.OK_SSL || code == ResultCode.OK_NO_SSL ||
                code == ResultCode.OK_REDIRECT_TO_NON_SECURE_CONNECTION || code == ResultCode.ETAG_CHANGED ||
                code == ResultCode.ETAG_UNCHANGED);
        mData = null;
    }

    private RemoteOperationResult(boolean success, int httpCode) {
        mSuccess = success;
        mHttpCode = httpCode;

        if (success) {
            mCode = ResultCode.OK;

        } else if (httpCode > 0) {
            switch (httpCode) {
            case HttpStatus.SC_UNAUTHORIZED:
                mCode = ResultCode.UNAUTHORIZED;
                break;
            case HttpStatus.SC_NOT_FOUND:
                mCode = ResultCode.FILE_NOT_FOUND;
                break;
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                mCode = ResultCode.INSTANCE_NOT_CONFIGURED;
                break;
            case HttpStatus.SC_CONFLICT:
                mCode = ResultCode.CONFLICT;
                break;
            case HttpStatus.SC_INSUFFICIENT_STORAGE:
                mCode = ResultCode.QUOTA_EXCEEDED;
                break;
			case HttpStatus.SC_FORBIDDEN:
				mCode = ResultCode.FORBIDDEN;
                break;
            case HttpStatus.SC_SERVICE_UNAVAILABLE:
                mCode = ResultCode.MAINTENANCE_MODE;
                break;
            default:
                mCode = ResultCode.UNHANDLED_HTTP_CODE;
                Log_OC.d(TAG, "RemoteOperationResult has processed UNHANDLED_HTTP_CODE: " + httpCode);
            }
        }
    }

    public RemoteOperationResult(boolean success, int httpCode, Header[] headers) {
        this(success, httpCode);

        if (headers != null) {
            for (Header header : headers) {
                if (HEADER_LOCATION.equals(header.getName().toLowerCase(Locale.US))) {
                    mRedirectedLocation = header.getValue();

                } else if (HEADER_WWW_AUTHENTICATE.equals(header.getName().toLowerCase(Locale.US))) {
                    mAuthenticateHeaders.add(header.getValue());
                }
            }
        }
        if (isIdPRedirection()) {
            mCode = ResultCode.UNAUTHORIZED;    // overrides default ResultCode.UNKNOWN
        }
    }

    public RemoteOperationResult(boolean success, String bodyResponse, int httpCode) {
        mSuccess = success;
        mHttpCode = httpCode;

        if (success) {
            mCode = ResultCode.OK;
        } else if (httpCode > 0) {
            switch (httpCode) {
                case HttpStatus.SC_BAD_REQUEST:
                    try {
                        InputStream is = new ByteArrayInputStream(bodyResponse.getBytes());
                        ExceptionParser xmlParser = new ExceptionParser(is);
                        if (xmlParser.isInvalidCharacterException()) {
                            mCode = ResultCode.INVALID_CHARACTER_DETECT_IN_SERVER;
                        }
                    } catch (Exception e) {
                        mCode = ResultCode.UNHANDLED_HTTP_CODE;
                        Log_OC.e(TAG, "Exception reading exception from server", e);
                    }
                    break;
                default:
                    mCode = ResultCode.UNHANDLED_HTTP_CODE;
                    Log_OC.d(TAG, "RemoteOperationResult has processed UNHANDLED_HTTP_CODE: " + httpCode);
            }
        }
    }

    /**
     * Public constructor from exception.
     *
     * To be used when an exception prevented the end of the {@link RemoteOperation}.
     *
     * Determines a {@link ResultCode} depending on the type of the exception.
     *
     * @param e Exception that interrupted the {@link RemoteOperation}
     */
    public RemoteOperationResult(Exception e) {
        mException = e;

        if (e instanceof OperationCancelledException) {
            mCode = ResultCode.CANCELLED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && e instanceof ErrnoException && ((ErrnoException) e).errno == OsConstants.ENOTCONN) {
            mCode = ResultCode.NO_NETWORK_CONNECTION;
        } else if (e instanceof ConnectException) {
            mCode = ResultCode.HOST_NOT_AVAILABLE;
        } else if (e instanceof SocketException) {
            mCode = ResultCode.WRONG_CONNECTION;
        } else if (e instanceof SocketTimeoutException) {
            mCode = ResultCode.TIMEOUT;
        } else if (e instanceof ConnectTimeoutException) {
            mCode = ResultCode.TIMEOUT;
        } else if (e instanceof MalformedURLException) {
            mCode = ResultCode.INCORRECT_ADDRESS;
        } else if (e instanceof UnknownHostException) {
            mCode = ResultCode.HOST_NOT_AVAILABLE;
        } else if (e instanceof AccountNotFoundException) {
            mCode = ResultCode.ACCOUNT_NOT_FOUND;
        } else if (e instanceof AccountsException) {
            mCode = ResultCode.ACCOUNT_EXCEPTION;
        } else if (e instanceof SSLException || e instanceof RuntimeException) {
            CertificateCombinedException se = getCertificateCombinedException(e);
            if (se != null) {
                mException = se;
                if (se.isRecoverable()) {
                    mCode = ResultCode.SSL_RECOVERABLE_PEER_UNVERIFIED;
                }
            } else if (e instanceof RuntimeException) {
                mCode = ResultCode.HOST_NOT_AVAILABLE;

            } else {
                mCode = ResultCode.SSL_ERROR;
            }
        } else if (e instanceof FileNotFoundException) {
            mCode = ResultCode.LOCAL_FILE_NOT_FOUND;
        } else {
            mCode = ResultCode.UNKNOWN_ERROR;
        }
    }

    public RemoteOperationResult(boolean success, OkHttpMethodBase httpMethod) {
        this(success, httpMethod.getStatusCode(), httpMethod.getStatusText(), httpMethod.getResponseHeaders());
    }

    /**
     * Public constructor from separate elements of an HTTP or DAV response.
     *
     * To be used when the result needs to be interpreted from the response of an HTTP/DAV method.
     *
     * Determines a {@link ResultCode} from the already executed method received as a parameter. Generally,
     * will depend on the HTTP code and HTTP response headers received. In some cases will inspect also the
     * response body.
     *
     * @param success    The operation was considered successful or not.
     * @param httpMethod HTTP/DAV method already executed which response will be examined to interpret the
     *                   result.
     */
    public RemoteOperationResult(boolean success, HttpMethod httpMethod) {
        this(success, httpMethod.getStatusCode(), httpMethod.getStatusText(), httpMethod.getResponseHeaders());

        if (mHttpCode == HttpStatus.SC_BAD_REQUEST || mHttpCode == HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE) {
            try {
                String bodyResponse = httpMethod.getResponseBodyAsString();

                if (bodyResponse != null && bodyResponse.length() > 0) {
                    InputStream is = new ByteArrayInputStream(bodyResponse.getBytes());
                    ExceptionParser xmlParser = new ExceptionParser(is);

                    if (xmlParser.isInvalidCharacterException()) {
                        mCode = ResultCode.INVALID_CHARACTER_DETECT_IN_SERVER;
                    }
                    if (xmlParser.isVirusException()) {
                        mCode = ResultCode.VIRUS_DETECTED;
                    }

                    mHttpPhrase = xmlParser.getMessage();
                }
            } catch (Exception e) {
                Log_OC.w(TAG, "Error reading exception from server: " + e.getMessage());
                // mCode stays as set in this(success, httpCode, headers)
            }
        }
    }

    /**
     * Public constructor from separate elements of an HTTP or DAV response.
     *
     * To be used when the result needs to be interpreted from HTTP response elements that could come from
     * different requests (WARNING: black magic, try to avoid).
     *
     * If all the fields come from the same HTTP/DAV response, {@link #RemoteOperationResult(boolean, HttpMethod)}
     * should be used instead.
     *
     * Determines a {@link ResultCode} depending on the HTTP code and HTTP response headers received.
     *
     * @param success     The operation was considered successful or not.
     * @param httpCode    HTTP status code returned by an HTTP/DAV method.
     * @param httpPhrase  HTTP status line phrase returned by an HTTP/DAV method
     * @param httpHeaders HTTP response header returned by an HTTP/DAV method
     */
    public RemoteOperationResult(boolean success, int httpCode, String httpPhrase, Header[] httpHeaders) {
        this(success, httpCode, httpPhrase);
        if (httpHeaders != null) {
            Header current;
            for (Header httpHeader : httpHeaders) {
                current = httpHeader;
                if (HEADER_WWW_AUTHENTICATE.equals(current.getName().toLowerCase(Locale.US))) {
                    mAuthenticateHeaders.add(current.getValue());
                } else if (HEADER_LOCATION.equals(current.getName().toLowerCase(Locale.US)) && mAuthenticateHeaders.isEmpty()) {
                    mRedirectedLocation = current.getValue();
                }
            }
        }
        if (isIdPRedirection()) {
            mCode = ResultCode.UNAUTHORIZED;    // overrides default ResultCode.UNKNOWN
        }
    }

    /**
     * Public constructor from separate elements of an HTTP or DAV response.
     *
     * To be used when the result needs to be interpreted from HTTP response elements that could come from
     * different requests (WARNING: black magic, try to avoid).
     *
     * If all the fields come from the same HTTP/DAV response, {@link #RemoteOperationResult(boolean, HttpMethod)}
     * should be used instead.
     *
     * Determines a {@link ResultCode} depending on the HTTP code and HTTP response headers received.
     *
     * @param success     The operation was considered successful or not.
     * @param httpCode    HTTP status code returned by an HTTP/DAV method.
     * @param httpPhrase  HTTP status line phrase returned by an HTTP/DAV method
     * @param httpHeaders HTTP response header returned by an HTTP/DAV method
     */
    public RemoteOperationResult(boolean success,
                                 int httpCode,
                                 String httpPhrase,
                                 Headers httpHeaders) {
        this(success, httpCode, httpPhrase);

        String location = httpHeaders.get(HEADER_LOCATION);
        if (location != null) {
            mRedirectedLocation = location;
        }

        String auth = httpHeaders.get(HEADER_WWW_AUTHENTICATE);
        if (auth != null) {
            mAuthenticateHeaders.add(auth);
        }

        if (isIdPRedirection()) {
            mCode = ResultCode.UNAUTHORIZED;    // overrides default ResultCode.UNKNOWN
        }
    }

    /**
     * Private constructor for results built interpreting a HTTP or DAV response.
     *
     * Determines a {@link ResultCode} depending of the type of the exception.
     *
     * @param success    Operation was successful or not.
     * @param httpCode   HTTP status code returned by the HTTP/DAV method.
     * @param httpPhrase HTTP status line phrase returned by the HTTP/DAV method
     */
    private RemoteOperationResult(boolean success, int httpCode, String httpPhrase) {
        mSuccess = success;
        mHttpCode = httpCode;
        mHttpPhrase = httpPhrase;

        if (success) {
            mCode = ResultCode.OK;

        } else if (httpCode > 0) {
            switch (httpCode) {
                case HttpStatus.SC_UNAUTHORIZED:                    // 401
                    mCode = ResultCode.UNAUTHORIZED;
                    break;
                case HttpStatus.SC_FORBIDDEN:                       // 403
                    mCode = ResultCode.FORBIDDEN;
                    break;
                case HttpStatus.SC_NOT_FOUND:                       // 404
                    mCode = ResultCode.FILE_NOT_FOUND;
                    break;
                case HttpStatus.SC_CONFLICT:                        // 409
                    mCode = ResultCode.CONFLICT;
                    break;
                case HttpStatus.SC_INTERNAL_SERVER_ERROR:           // 500
                    mCode = ResultCode.INSTANCE_NOT_CONFIGURED;     // assuming too much...
                    break;
                case HttpStatus.SC_SERVICE_UNAVAILABLE:             // 503
                    mCode = ResultCode.MAINTENANCE_MODE;
                    break;
                case HttpStatus.SC_INSUFFICIENT_STORAGE:            // 507
                    mCode = ResultCode.QUOTA_EXCEEDED;
                    break;
                default:
                    mCode = ResultCode.UNHANDLED_HTTP_CODE;         // UNKNOWN ERROR
                    Log_OC.d(TAG,
                            "RemoteOperationResult has processed UNHANDLED_HTTP_CODE: "
                                    + mHttpCode + " " + mHttpPhrase);
            }
        }
    }

    /**
     * @deprecated use setResultData() instead
     */
    @Deprecated
    public void setData(ArrayList<Object> files) {
        mData = files;
    }

    /**
     * @deprecated use setResultData() instead
     */
    @Deprecated
    public void setSingleData(Object object) {
        mData = new ArrayList<>(Collections.singletonList(object));
    }

    public void setResultData(T object) {
        resultData = object;
    }

    public T getResultData() {
        if (!mSuccess) {
            throw new RuntimeException("Accessing result data after operation failed!");
        }
        return resultData;
    }

    /**
     * @deprecated use getResultData() instead
     */
    @Deprecated
    public ArrayList<Object> getData() {
        if (!mSuccess) {
            throw new RuntimeException("Accessing result data after operation failed!");
        }
        if (mData != null) {
            return mData;
        } else if (resultData instanceof ArrayList) {
            return (ArrayList) resultData;
        } else {
            return null;
        }
    }

    /**
     * @deprecated use getResultData() instead
     */
    @Deprecated
    public Object getSingleData() {
        if (!mSuccess) {
            throw new RuntimeException("Accessing result data after operation failed!");
        }
        return mData.get(0);
    }

    /**
     * @deprecated use getResultData() instead
     */
    public void setNotificationData(List<Notification> notifications) {
        mNotificationData = notifications;
    }

    /**
     * @deprecated use getResultData() instead
     */
    public PushResponse getPushResponseData() {
        if (!mSuccess) {
            throw new RuntimeException("Accessing result data after operation failed!");
        }
        return mPushResponse;
    }

    /**
     * @deprecated use getResultData() instead
     */
    public void setPushResponseData(PushResponse pushResponseData) {
        mPushResponse = pushResponseData;
    }

    /**
     * @deprecated use getResultData() instead
     */
    public List<Notification> getNotificationData() {
        if (!mSuccess) {
            throw new RuntimeException("Accessing result data after operation failed!");
        }
        return mNotificationData;
    }


    public boolean isSuccess() {
        return mSuccess;
    }

    public boolean isCancelled() {
        return mCode == ResultCode.CANCELLED;
    }

    public int getHttpCode() {
        return mHttpCode;
    }

    public String getHttpPhrase() {
        return mHttpPhrase;
    }

    public ResultCode getCode() {
        return mCode;
    }

    public Exception getException() {
        return mException;
    }

    public boolean isSslRecoverableException() {
        return mCode == ResultCode.SSL_RECOVERABLE_PEER_UNVERIFIED;
    }

    public boolean isRedirectToNonSecureConnection() {
        return mCode == ResultCode.OK_REDIRECT_TO_NON_SECURE_CONNECTION;
    }

    private CertificateCombinedException getCertificateCombinedException(Exception e) {
        CertificateCombinedException result = null;
        if (e instanceof CertificateCombinedException) {
            return (CertificateCombinedException) e;
        }
        Throwable cause = mException.getCause();
        Throwable previousCause = null;
        while (cause != null && cause != previousCause &&
                !(cause instanceof CertificateCombinedException)) {
            previousCause = cause;
            cause = cause.getCause();
        }
        if (cause instanceof CertificateCombinedException) {
            result = (CertificateCombinedException) cause;
        }
        return result;
    }

    public String getLogMessage() {

        if (mException != null) {
            if (mException instanceof OperationCancelledException) {
                return "Operation cancelled by the caller";

            } else if (mException instanceof SocketException) {
                return "Socket exception";

            } else if (mException instanceof SocketTimeoutException) {
                return "Socket timeout exception";

            } else if (mException instanceof ConnectTimeoutException) {
                return "Connect timeout exception";

            } else if (mException instanceof MalformedURLException) {
                return "Malformed URL exception";

            } else if (mException instanceof UnknownHostException) {
                return "Unknown host exception";

            } else if (mException instanceof CertificateCombinedException) {
                if (((CertificateCombinedException) mException).isRecoverable())
                    return "SSL recoverable exception";
                else
                    return "SSL exception";

            } else if (mException instanceof SSLException) {
                return "SSL exception";

            } else if (mException instanceof DavException) {
                return "Unexpected WebDAV exception";

            } else if (mException instanceof HttpException) {
                return "HTTP violation";

            } else if (mException instanceof IOException) {
                return "Unrecovered transport exception";

            } else if (mException instanceof AccountNotFoundException) {
                Account failedAccount =
                        ((AccountNotFoundException)mException).getFailedAccount();
                return mException.getMessage() + " (" +
                        (failedAccount != null ? failedAccount.name : "NULL") + ")";
                
            } else if (mException instanceof AccountsException) {
                return "Exception while using account";

            } else if (mException instanceof JSONException) {
                return "JSON exception";

            } else {
                return "Unexpected exception";
            }
        }

        if (mCode == ResultCode.INSTANCE_NOT_CONFIGURED) {
            return "The Nextcloud server is not configured!";

        } else if (mCode == ResultCode.NO_NETWORK_CONNECTION) {
            return "No network connection";

        } else if (mCode == ResultCode.BAD_OC_VERSION) {
            return "No valid Nextcloud version was found at the server";

        } else if (mCode == ResultCode.LOCAL_STORAGE_FULL) {
            return "Local storage full";

        } else if (mCode == ResultCode.LOCAL_STORAGE_NOT_MOVED) {
            return "Error while moving file to final directory";

        } else if (mCode == ResultCode.ACCOUNT_NOT_NEW) {
            return "Account already existing when creating a new one";

        } else if (mCode == ResultCode.ACCOUNT_NOT_THE_SAME) {
            return "Authenticated with a different account than the one updating";

        } else if (mCode == ResultCode.INVALID_CHARACTER_IN_NAME) {
                return "The file name contains an forbidden character";

        } else if (mCode == ResultCode.FILE_NOT_FOUND) {
	  	    return "Local file does not exist";

 	    } else if (mCode == ResultCode.SYNC_CONFLICT) {
            return "Synchronization conflict";
        }

        return "Operation finished with HTTP status code " + mHttpCode + " (" +
                (isSuccess() ? "success" : "fail") + ")";

    }

    public boolean isServerFail() {
        return (mHttpCode >= HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    public boolean isException() {
        return (mException != null);
    }

    public boolean isTemporalRedirection() {
        return (mHttpCode == 302 || mHttpCode == 307);
    }

    public String getRedirectedLocation() {
        return mRedirectedLocation;
    }

    public final boolean isIdPRedirection() {
        return (mRedirectedLocation != null &&
                (mRedirectedLocation.toUpperCase(Locale.US).contains("SAML") ||
                        mRedirectedLocation.toLowerCase(Locale.US).contains("wayf")));
    }

    /**
     * Checks if is a non https connection
     *
     * @return boolean true/false
     */
    public boolean isNonSecureRedirection() {
        return (mRedirectedLocation != null && !(mRedirectedLocation.toLowerCase(Locale.US).startsWith("https://")));
    }

    public ArrayList<String> getAuthenticateHeaders() {
        return mAuthenticateHeaders;
    }

    public String getLastPermanentLocation() {
        return mLastPermanentLocation;
    }

    public void setLastPermanentLocation(String lastPermanentLocation) {
        mLastPermanentLocation = lastPermanentLocation;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Message that is returned by server, e.g. password policy violation on ocs share api
     * @return message that can be shown to user
     */
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "RemoteOperationResult{" +
                "mSuccess=" + mSuccess +
                ", mHttpCode=" + mHttpCode +
                ", mHttpPhrase='" + mHttpPhrase + '\'' +
                ", mException=" + mException +
                ", mCode=" + mCode +
                ", message='" + message + '\'' +
                ", getLogMessage='" + getLogMessage() + '\'' +
                '}';
    }
}
