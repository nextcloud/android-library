/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Álvaro Brey <alvaro.brey@nextcloud.com>
 * SPDX-FileCopyrightText: 2019-2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2017 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2014-2016 ownCloud Inc.
 * SPDX-FileCopyrightText: 2015 masensio <masensio@solidgear.es>
 * SPDX-FileCopyrightText: 2014 David A. Velasco <dvelasco@solidgear.es>
 * SPDX-FileCopyrightText: 2014 Jorge Antonio Diaz-Benito Soriano <jorge.diazbenitosoriano@gmail.com>
 * SPDX-FileCopyrightText: 2014-2016 Juan Carlos González Cabrero <malkomich@gmail.com>
 * SPDX-FileCopyrightText: 2014 jabarros <jabarros@solidgear.es>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.common.operations

import android.accounts.AccountsException
import android.os.Build
import android.system.ErrnoException
import android.system.OsConstants
import com.nextcloud.common.DavResponse
import com.nextcloud.common.OkHttpMethodBase
import com.owncloud.android.lib.common.accounts.AccountUtils
import com.owncloud.android.lib.common.network.CertificateCombinedException
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.resources.files.CreateLocalFileException
import okhttp3.Headers
import okhttp3.internal.http.HTTP_MOVED_TEMP
import okhttp3.internal.http.HTTP_TEMP_REDIRECT
import org.apache.commons.httpclient.ConnectTimeoutException
import org.apache.commons.httpclient.Header
import org.apache.commons.httpclient.HttpException
import org.apache.commons.httpclient.HttpMethod
import org.apache.commons.httpclient.HttpStatus
import org.apache.jackrabbit.webdav.DavException
import org.json.JSONException
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.Serial
import java.io.Serializable
import java.net.ConnectException
import java.net.MalformedURLException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * The result of a remote operation required to an ownCloud server.
 *
 *
 * Provides a common classification of remote operation results for all the application.
 *
 * @author David A. Velasco
 */
class RemoteOperationResult<T> : Serializable {
    enum class ResultCode {
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
        CANNOT_CREATE_FILE,
        LOCKED
    }

    var isSuccess = false
        private set
    var httpCode = -1
        private set
    var httpPhrase: String? = null
        private set
    var exception: Exception? = null
        private set
    var code = ResultCode.UNKNOWN_ERROR
        private set
    var lastPermanentLocation: String? = null

    /**
     * Message that is returned by server, e.g. password policy violation on ocs share api
     * @return message that can be shown to user
     */
    @JvmField
    var message: String? = null
    var redirectedLocation: String? = null
        private set
    val authenticateHeaders = ArrayList<String>()
    private var mData: ArrayList<Any>? = null

    @Suppress("TooGenericExceptionThrown")
    var resultData: T? = null
        get() {
            if (isSuccess) {
                return field
            }
            throw RuntimeException("Accessing result data after operation failed!")
        }

    /**
     * Public constructor from result code.
     *
     *
     * To be used when the caller takes the responsibility of interpreting the result of a [RemoteOperation]
     *
     * @param code [ResultCode] decided by the caller.
     */
    constructor(code: ResultCode) {
        this.code = code
        isSuccess = code in
            listOf(
                ResultCode.OK,
                ResultCode.OK_SSL,
                ResultCode.OK_NO_SSL,
                ResultCode.OK_REDIRECT_TO_NON_SECURE_CONNECTION,
                ResultCode.ETAG_CHANGED,
                ResultCode.ETAG_UNCHANGED
            )
        mData = null
    }

    private constructor(success: Boolean, httpCode: Int) {
        isSuccess = success
        this.httpCode = httpCode
        if (success) {
            this.code = ResultCode.OK
        } else if (httpCode > 0) {
            this.code =
                when (httpCode) {
                    HttpStatus.SC_UNAUTHORIZED -> ResultCode.UNAUTHORIZED
                    HttpStatus.SC_NOT_FOUND -> ResultCode.FILE_NOT_FOUND
                    HttpStatus.SC_INTERNAL_SERVER_ERROR -> ResultCode.INSTANCE_NOT_CONFIGURED
                    HttpStatus.SC_CONFLICT -> ResultCode.CONFLICT
                    HttpStatus.SC_INSUFFICIENT_STORAGE -> ResultCode.QUOTA_EXCEEDED
                    HttpStatus.SC_FORBIDDEN -> ResultCode.FORBIDDEN
                    HttpStatus.SC_SERVICE_UNAVAILABLE -> ResultCode.MAINTENANCE_MODE
                    HttpStatus.SC_LOCKED -> ResultCode.LOCKED
                    else -> {
                        Log_OC.d(TAG, "RemoteOperationResult has processed UNHANDLED_HTTP_CODE: $httpCode")
                        ResultCode.UNHANDLED_HTTP_CODE
                    }
                }
        }
    }

    constructor(success: Boolean, bodyResponse: String, httpCode: Int) {
        isSuccess = success
        this.httpCode = httpCode
        if (success) {
            this.code = ResultCode.OK
        } else if (httpCode > 0) {
            if (httpCode == HttpStatus.SC_BAD_REQUEST) {
                try {
                    val inputStream: InputStream = ByteArrayInputStream(bodyResponse.toByteArray())
                    val xmlParser = ExceptionParser(inputStream)
                    if (xmlParser.isInvalidCharacterException) {
                        this.code = ResultCode.INVALID_CHARACTER_DETECT_IN_SERVER
                    }
                } catch (e: IOException) {
                    this.code = ResultCode.UNHANDLED_HTTP_CODE
                    Log_OC.e(TAG, "Exception reading exception from server", e)
                }
            } else {
                this.code = ResultCode.UNHANDLED_HTTP_CODE
                Log_OC.d(TAG, "RemoteOperationResult has processed UNHANDLED_HTTP_CODE: $httpCode")
            }
        }
    }

    /**
     * Public constructor from exception.
     *
     *
     * To be used when an exception prevented the end of the [RemoteOperation].
     *
     *
     * Determines a [ResultCode] depending on the type of the exception.
     *
     * @param e Exception that interrupted the [RemoteOperation]
     */
    constructor(e: Exception?) {
        exception = e
        if (e is OperationCancelledException) {
            this.code = ResultCode.CANCELLED
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
            e is ErrnoException && e.errno == OsConstants.ENOTCONN
        ) {
            this.code = ResultCode.NO_NETWORK_CONNECTION
        } else if (e is ConnectException) {
            this.code = ResultCode.HOST_NOT_AVAILABLE
        } else if (e is SocketException) {
            this.code = ResultCode.WRONG_CONNECTION
        } else if (e is SocketTimeoutException) {
            this.code = ResultCode.TIMEOUT
        } else if (e is ConnectTimeoutException) {
            this.code = ResultCode.TIMEOUT
        } else if (e is MalformedURLException) {
            this.code = ResultCode.INCORRECT_ADDRESS
        } else if (e is UnknownHostException) {
            this.code = ResultCode.HOST_NOT_AVAILABLE
        } else if (e is AccountUtils.AccountNotFoundException) {
            this.code = ResultCode.ACCOUNT_NOT_FOUND
        } else if (e is AccountsException) {
            this.code = ResultCode.ACCOUNT_EXCEPTION
        } else if (e is SSLException || e is RuntimeException) {
            val certificateCombinedException = getCertificateCombinedException(e)
            if (certificateCombinedException != null) {
                exception = certificateCombinedException
                if (certificateCombinedException.isRecoverable) {
                    this.code = ResultCode.SSL_RECOVERABLE_PEER_UNVERIFIED
                }
            } else if (e is RuntimeException) {
                this.code = ResultCode.HOST_NOT_AVAILABLE
            } else {
                this.code = ResultCode.SSL_ERROR
            }
        } else if (e is FileNotFoundException) {
            this.code = ResultCode.LOCAL_FILE_NOT_FOUND
        } else if (e is CreateLocalFileException) {
            if (e.isCausedByInvalidPath()) {
                this.code = ResultCode.INVALID_LOCAL_FILE_NAME
            } else {
                this.code = ResultCode.CANNOT_CREATE_FILE
            }
        } else {
            this.code = ResultCode.UNKNOWN_ERROR
        }
    }

    constructor(success: Boolean, httpMethod: OkHttpMethodBase) : this(
        success,
        httpMethod.getStatusCode(),
        httpMethod.getStatusText(),
        httpMethod.getResponseHeaders()
    )

    /**
     * Create RemoteOperationResult from given [DavResponse].
     *
     * Assumes "HTTP/1.1 200 OK" if status of [davResponse] is null.
     *
     * @param davResponse response received from remote dav operation
     */
    constructor(davResponse: DavResponse) : this(
        davResponse.success,
        davResponse.getStatusCode(),
        davResponse.status?.message ?: "HTTP/1.1 200 OK",
        davResponse.headers ?: Headers.headersOf()
    )

    /**
     * Public constructor from separate elements of an HTTP or DAV response.
     *
     *
     * To be used when the result needs to be interpreted from the response of an HTTP/DAV method.
     *
     *
     * Determines a [ResultCode] from the already executed method received as a parameter. Generally,
     * will depend on the HTTP code and HTTP response headers received. In some cases will inspect also the
     * response body.
     *
     * @param success    The operation was considered successful or not.
     * @param httpMethod HTTP/DAV method already executed which response will be examined to interpret the
     * result.
     */
    constructor(success: Boolean, httpMethod: HttpMethod) : this(
        success,
        httpMethod.statusCode,
        httpMethod.statusText,
        httpMethod.responseHeaders
    ) {
        if (httpCode == HttpStatus.SC_BAD_REQUEST || httpCode == HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE) {
            try {
                val bodyResponse = httpMethod.responseBodyAsString
                if (!bodyResponse.isNullOrEmpty()) {
                    val inputStream: InputStream = ByteArrayInputStream(bodyResponse.toByteArray())
                    val xmlParser = ExceptionParser(inputStream)
                    if (xmlParser.isInvalidCharacterException) {
                        this.code = ResultCode.INVALID_CHARACTER_DETECT_IN_SERVER
                    }
                    if (xmlParser.isVirusException) {
                        this.code = ResultCode.VIRUS_DETECTED
                    }
                    httpPhrase = xmlParser.message
                }
            } catch (e: IOException) {
                Log_OC.w(TAG, "Error reading exception from server: " + e.message)
                // mCode stays as set in this(success, httpCode, headers)
            }
        }
    }

    /**
     * Public constructor from separate elements of an HTTP or DAV response.
     *
     *
     * To be used when the result needs to be interpreted from HTTP response elements that could come from
     * different requests (WARNING: black magic, try to avoid).
     *
     *
     * If all the fields come from the same HTTP/DAV response, [.RemoteOperationResult]
     * should be used instead.
     *
     *
     * Determines a [ResultCode] depending on the HTTP code and HTTP response headers received.
     *
     * @param success     The operation was considered successful or not.
     * @param httpCode    HTTP status code returned by an HTTP/DAV method.
     * @param httpPhrase  HTTP status line phrase returned by an HTTP/DAV method
     * @param httpHeaders HTTP response header returned by an HTTP/DAV method
     */
    constructor(
        success: Boolean,
        httpCode: Int,
        httpPhrase: String,
        httpHeaders: Array<Header>?
    ) : this(success, httpCode, httpPhrase) {
        if (httpHeaders != null) {
            for (httpHeader in httpHeaders) {
                if (HEADER_WWW_AUTHENTICATE == httpHeader.name.lowercase()) {
                    authenticateHeaders.add(httpHeader.value)
                } else if (HEADER_LOCATION == httpHeader.name.lowercase() && authenticateHeaders.isEmpty()) {
                    redirectedLocation = httpHeader.value
                }
            }
        }
        if (isIdPRedirection) {
            this.code = ResultCode.UNAUTHORIZED // overrides default ResultCode.UNKNOWN
        }
    }

    constructor(success: Boolean, httpCode: Int, headers: Array<Header>?) : this(
        success,
        httpCode
    ) {
        if (headers != null) {
            for (header in headers) {
                if (HEADER_LOCATION == header.name.lowercase()) {
                    redirectedLocation = header.value
                } else if (HEADER_WWW_AUTHENTICATE == header.name.lowercase()) {
                    authenticateHeaders.add(header.value)
                }
            }
        }
        if (isIdPRedirection) {
            this.code = ResultCode.UNAUTHORIZED // overrides default ResultCode.UNKNOWN
        }
    }

    /**
     * Public constructor from separate elements of an HTTP or DAV response.
     *
     *
     * To be used when the result needs to be interpreted from HTTP response elements that could come from
     * different requests (WARNING: black magic, try to avoid).
     *
     *
     * If all the fields come from the same HTTP/DAV response, [.RemoteOperationResult]
     * should be used instead.
     *
     *
     * Determines a [ResultCode] depending on the HTTP code and HTTP response headers received.
     *
     * @param success     The operation was considered successful or not.
     * @param httpCode    HTTP status code returned by an HTTP/DAV method.
     * @param httpPhrase  HTTP status line phrase returned by an HTTP/DAV method
     * @param httpHeaders HTTP response header returned by an HTTP/DAV method
     */
    constructor(
        success: Boolean,
        httpCode: Int,
        httpPhrase: String,
        httpHeaders: Headers
    ) : this(success, httpCode, httpPhrase) {
        val location = httpHeaders[HEADER_LOCATION]
        if (location != null) {
            redirectedLocation = location
        }
        val auth = httpHeaders[HEADER_WWW_AUTHENTICATE]
        if (auth != null) {
            authenticateHeaders.add(auth)
        }
        if (isIdPRedirection) {
            this.code = ResultCode.UNAUTHORIZED // overrides default ResultCode.UNKNOWN
        }
    }

    /**
     * Private constructor for results built interpreting a HTTP or DAV response.
     *
     *
     * Determines a [ResultCode] depending of the type of the exception.
     *
     * @param success    Operation was successful or not.
     * @param httpCode   HTTP status code returned by the HTTP/DAV method.
     * @param httpPhrase HTTP status line phrase returned by the HTTP/DAV method
     */
    private constructor(success: Boolean, httpCode: Int, httpPhrase: String) {
        isSuccess = success
        this.httpCode = httpCode
        this.httpPhrase = httpPhrase
        if (success) {
            this.code = ResultCode.OK
        } else if (httpCode > 0) {
            when (httpCode) {
                HttpStatus.SC_UNAUTHORIZED -> // 401
                    this.code = ResultCode.UNAUTHORIZED

                HttpStatus.SC_FORBIDDEN -> // 403
                    this.code = ResultCode.FORBIDDEN

                HttpStatus.SC_NOT_FOUND -> // 404
                    this.code = ResultCode.FILE_NOT_FOUND

                HttpStatus.SC_CONFLICT -> // 409
                    this.code = ResultCode.CONFLICT

                HttpStatus.SC_LOCKED -> // 423
                    this.code = ResultCode.LOCKED

                HttpStatus.SC_INTERNAL_SERVER_ERROR -> // 500
                    this.code = ResultCode.INSTANCE_NOT_CONFIGURED

                HttpStatus.SC_SERVICE_UNAVAILABLE -> // 503
                    this.code = ResultCode.MAINTENANCE_MODE

                HttpStatus.SC_INSUFFICIENT_STORAGE -> // 507
                    this.code = ResultCode.QUOTA_EXCEEDED

                else -> {
                    this.code = ResultCode.UNHANDLED_HTTP_CODE // UNKNOWN ERROR
                    Log_OC.d(
                        TAG,
                        "RemoteOperationResult has processed UNHANDLED_HTTP_CODE: ${this.httpCode} ${this.httpPhrase}"
                    )
                }
            }
        }
    }

    val isCancelled = code == ResultCode.CANCELLED

    val isSslRecoverableException = code == ResultCode.SSL_RECOVERABLE_PEER_UNVERIFIED

    val isRedirectToNonSecureConnection = code == ResultCode.OK_REDIRECT_TO_NON_SECURE_CONNECTION

    private fun getCertificateCombinedException(e: Exception): CertificateCombinedException? {
        var result: CertificateCombinedException? = null
        if (e is CertificateCombinedException) {
            return e
        }
        var cause = exception!!.cause
        var previousCause: Throwable? = null
        while (cause != null && cause !== previousCause &&
            cause !is CertificateCombinedException
        ) {
            previousCause = cause
            cause = cause.cause
        }
        if (cause is CertificateCombinedException) {
            result = cause
        }
        return result
    }

    val logMessage: String
        get() {
            exception?.let { exception ->
                return when (exception) {
                    is OperationCancelledException -> "Operation cancelled by the caller"
                    is SocketException -> "Socket exception"
                    is SocketTimeoutException -> "Socket timeout exception"
                    is ConnectTimeoutException -> "Connect timeout exception"
                    is MalformedURLException -> "Malformed URL exception"
                    is UnknownHostException -> "Unknown host exception"
                    is CertificateCombinedException ->
                        if (exception.isRecoverable) "SSL recoverable exception" else "SSL exception"
                    is SSLException -> "SSL exception"
                    is DavException -> "Unexpected WebDAV exception"
                    is HttpException -> "HTTP violation"
                    is IOException -> "Unrecovered transport exception"
                    is AccountUtils.AccountNotFoundException ->
                        "${exception.message} (${exception.failedAccount?.name ?: "NULL"})"
                    is AccountsException -> "Exception while using account"
                    is JSONException -> "JSON exception"
                    else -> "Unexpected exception"
                }
            }
            return when (code) {
                ResultCode.INSTANCE_NOT_CONFIGURED -> "The Nextcloud server is not configured!"
                ResultCode.NO_NETWORK_CONNECTION -> "No network connection"
                ResultCode.BAD_OC_VERSION -> "No valid Nextcloud version was found at the server"
                ResultCode.LOCAL_STORAGE_FULL -> "Local storage full"
                ResultCode.LOCAL_STORAGE_NOT_MOVED -> "Error while moving file to final directory"
                ResultCode.ACCOUNT_NOT_NEW -> "Account already existing when creating a new one"
                ResultCode.ACCOUNT_NOT_THE_SAME -> "Authenticated with a different account than the one updating"
                ResultCode.INVALID_CHARACTER_IN_NAME -> "The file name contains an forbidden character"
                ResultCode.FILE_NOT_FOUND -> "Local file does not exist"
                ResultCode.SYNC_CONFLICT -> "Synchronization conflict"
                else -> "Operation finished with HTTP status code $httpCode (${if (isSuccess) "success" else "fail"})"
            }
        }

    val isServerFail = httpCode >= HttpStatus.SC_INTERNAL_SERVER_ERROR

    val isException = exception != null

    val isTemporalRedirection = httpCode == HTTP_MOVED_TEMP || httpCode == HTTP_TEMP_REDIRECT

    val isIdPRedirection =
        redirectedLocation != null &&
            (
                redirectedLocation!!.uppercase().contains("SAML") ||
                    redirectedLocation!!.lowercase().contains("wayf")
            )

    /**
     * Checks if is a non https connection
     */
    val isNonSecureRedirection = redirectedLocation != null && !redirectedLocation!!.lowercase().startsWith("https://")

    override fun toString(): String =
        "RemoteOperationResult{mSuccess=$isSuccess, mHttpCode=$httpCode," + "mHttpPhrase='$httpPhrase', " +
            "mException=$exception, mCode=${this.code}, message='$message', getLogMessage='$logMessage'}"

    companion object {
        // Generated - should be refreshed every time the class changes!!
        @Serial
        private val serialVersionUID = -4325446958558896222L
        private val TAG = RemoteOperationResult::class.java.simpleName
        private const val HEADER_WWW_AUTHENTICATE = "www-authenticate"
        private const val HEADER_LOCATION = "location"
    }
}
