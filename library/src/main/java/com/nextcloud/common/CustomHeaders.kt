/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.common

import android.net.Uri
import com.owncloud.android.lib.common.utils.Log_OC
import okhttp3.HttpUrl
import org.apache.commons.httpclient.HttpMethod
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

/**
 * Process wide registry of additional request headers, each bound to one https origin.
 *
 * All library clients ask this registry on every hop (including redirects), so a header is only ever sent to the
 * origin it was registered for, for example a pre-authentication proxy in front of the server.
 *
 * The registry lives in memory only. Persisting the headers, for example as account user data with [toJson], and
 * registering them again after a process start is up to the app.
 */
object CustomHeaders {
    private const val TAG = "CustomHeaders"
    private const val HTTPS = "https"
    private const val DEFAULT_HTTPS_PORT = 443
    private const val MIN_VISIBLE_ASCII = 0x20
    private const val MAX_VISIBLE_ASCII = 0x7E
    private const val TAB = '\t'.code
    private const val INJECTED_HEADERS_PARAMETER = "nextcloud.customHeaders.injected"
    private val TOKEN = Regex("^[!#$%&'*+.^_`|~0-9A-Za-z-]+$")
    private val RESERVED_NAMES =
        setOf(
            "host",
            "authorization",
            "proxy-authorization",
            "user-agent",
            "cookie",
            "content-length",
            "content-type",
            "transfer-encoding",
            "connection",
            "destination"
        )

    @Volatile
    private var byOrigin: Map<String, Map<String, String>> = emptyMap()

    val origins: Set<String>
        get() = byOrigin.keys

    @JvmStatic
    fun isValid(
        name: String,
        value: String
    ): Boolean {
        val nameAllowed = TOKEN.matches(name) && name.lowercase(Locale.ROOT) !in RESERVED_NAMES
        val valueAllowed =
            value.isNotEmpty() &&
                value.all { it.code in MIN_VISIBLE_ASCII..MAX_VISIBLE_ASCII || it.code == TAB }
        return nameAllowed && valueAllowed
    }

    /**
     * Same form as `URL.origin` in JavaScript: lower case, internationalized hosts in punycode as OkHttp sends them,
     * default port omitted.
     */
    @JvmStatic
    fun originOf(
        scheme: String?,
        host: String?,
        port: Int
    ): String? {
        val canonicalHost = host?.takeIf { it.isNotEmpty() }?.let { canonicalHostOf(it) }
        if (scheme == null || canonicalHost == null) {
            return null
        }
        val normalizedScheme = scheme.lowercase(Locale.ROOT)
        val bracketedHost = if (':' in canonicalHost) "[$canonicalHost]" else canonicalHost
        val isDefaultPort = port < 0 || (normalizedScheme == HTTPS && port == DEFAULT_HTTPS_PORT)
        return if (isDefaultPort) "$normalizedScheme://$bracketedHost" else "$normalizedScheme://$bracketedHost:$port"
    }

    private fun canonicalHostOf(host: String): String? =
        try {
            HttpUrl
                .Builder()
                .scheme(HTTPS)
                .host(host)
                .build()
                .host
        } catch (_: IllegalArgumentException) {
            null
        }

    @JvmStatic
    fun originOf(uri: Uri?): String? = uri?.let { originOf(it.scheme, it.host, it.port) }

    /**
     * @return false if the base URI is not https or a header is invalid; nothing is registered then
     */
    @JvmStatic
    fun register(
        baseUri: Uri?,
        headers: Map<String, String>
    ): Boolean {
        val isHttps = baseUri?.scheme.equals(HTTPS, ignoreCase = true)
        val origin = originOf(baseUri)
        if (!isHttps || origin == null) {
            return false
        }
        return register(origin, headers)
    }

    internal fun register(
        origin: String,
        headers: Map<String, String>
    ): Boolean {
        if (headers.isEmpty() || !headers.all { (name, value) -> isValid(name, value) }) {
            return false
        }
        synchronized(this) { byOrigin = byOrigin + (origin to headers.toMap()) }
        return true
    }

    @JvmStatic
    fun unregister(origin: String) {
        synchronized(this) { byOrigin = byOrigin - origin }
    }

    internal fun clear() {
        synchronized(this) { byOrigin = emptyMap() }
    }

    @JvmStatic
    fun forDestination(
        scheme: String?,
        host: String?,
        port: Int
    ): Map<String, String> = originOf(scheme, host, port)?.let { byOrigin[it] }.orEmpty()

    @JvmStatic
    fun forOrigin(origin: String): Map<String, String> = byOrigin[origin].orEmpty()

    /**
     * Apache methods are reused for every redirect hop, so the headers injected for the previous hop are removed by
     * name even if the registry changed in between, and values set by the operation itself are put back.
     */
    @JvmStatic
    fun applyTo(method: HttpMethod) {
        val previouslyInjected = method.params.getParameter(INJECTED_HEADERS_PARAMETER) as? Map<*, *>
        previouslyInjected?.forEach { (name, operationValue) ->
            method.removeRequestHeader(name as String)
            (operationValue as? String)?.let { method.setRequestHeader(name, it) }
        }

        val uri = method.uri
        val allowed = forDestination(uri.scheme, uri.host, uri.port)
        val operationValues = allowed.keys.associateWith { method.getRequestHeader(it)?.value }
        allowed.forEach { (name, value) -> method.setRequestHeader(name, value) }
        method.params.setParameter(INJECTED_HEADERS_PARAMETER, operationValues)
    }

    @JvmStatic
    fun toJson(headers: Map<String, String>): String = JSONObject(headers).toString()

    @JvmStatic
    fun fromJson(json: String?): Map<String, String>? {
        if (json.isNullOrBlank()) {
            return null
        }
        return try {
            val obj = JSONObject(json)
            obj.keys().asSequence().associateWith { obj.getString(it) }
        } catch (_: JSONException) {
            Log_OC.e(TAG, "custom headers are malformed and will be ignored")
            null
        }
    }
}
