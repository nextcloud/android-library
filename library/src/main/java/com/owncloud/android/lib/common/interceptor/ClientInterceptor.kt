/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: MIT
 */

package com.owncloud.android.lib.common.interceptor

import com.nextcloud.common.OkHttpMethodBase
import com.owncloud.android.lib.common.utils.Log_OC
import com.owncloud.android.lib.common.utils.responseFormat.ResponseFormat
import com.owncloud.android.lib.common.utils.responseFormat.ResponseFormatDetector
import okhttp3.Request
import okhttp3.Response
import org.apache.commons.httpclient.HttpMethod
import org.apache.commons.httpclient.HttpMethodBase
import org.json.JSONArray
import org.json.JSONObject
import org.xml.sax.InputSource
import java.io.StringReader
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.collections.component1
import kotlin.collections.component2

@Suppress("TooManyFunctions")
class ClientInterceptor {
    companion object {
        private const val TAG = "ClientInterceptor"
    }

    fun interceptHttpMethodBaseRequest(method: HttpMethodBase) {
        Log_OC.d(TAG, "➡️ Method: ${method.name} 🌐 URL: ${method.uri}")
        logHeaders(method.requestHeaders.map { it.name to it.value })

        if (method is org.apache.commons.httpclient.methods.EntityEnclosingMethod) {
            val buffer = java.io.ByteArrayOutputStream()
            method.requestEntity?.writeRequest(buffer)
            val body = buffer.toString(method.requestCharSet ?: Charsets.UTF_8.name())
            logBody(body, method.getRequestHeader("Content-Type")?.value, "Request")
        }
        Log_OC.d(TAG, "-------------------------")
    }

    fun interceptHttpMethodBaseResponse(
        method: HttpMethodBase,
        statusCode: Int
    ) {
        Log_OC.d(TAG, "⬅️ Status Code: $statusCode")
        logHeaders(method.responseHeaders.map { it.name to it.value })
        logBody(method.responseBodyAsString, method.getResponseHeader("Content-Type")?.value, "Response")
        Log_OC.d(TAG, "-------------------------")
    }

    fun interceptHttpMethodRequest(method: HttpMethod) {
        Log_OC.d(TAG, "➡️ Method: ${method.name} 🌐 URL: ${method.uri}")
        logHeaders(method.requestHeaders.map { it.name to it.value })

        if (method is org.apache.commons.httpclient.methods.EntityEnclosingMethod) {
            val buffer = java.io.ByteArrayOutputStream()
            method.requestEntity?.writeRequest(buffer)
            val body = buffer.toString(method.requestCharSet ?: Charsets.UTF_8.name())
            logBody(body, method.getRequestHeader("Content-Type")?.value, "Request")
        }
        Log_OC.d(TAG, "-------------------------")
    }

    fun interceptHttpMethodResponse(
        method: HttpMethod,
        statusCode: Int
    ) {
        Log_OC.d(TAG, "⬅️ Status Code: $statusCode")
        logHeaders(method.responseHeaders.map { it.name to it.value })
        logBody(method.responseBodyAsString, method.getResponseHeader("Content-Type")?.value, "Response")
        Log_OC.d(TAG, "-------------------------")
    }

    fun interceptOkHttp3Request(request: Request) {
        Log_OC.d(TAG, "➡️ Method: ${request.method} 🌐 URL: ${request.url}")
        request.headers?.toMultimap()?.let { headerMap ->
            logHeaders(headerMap.flatMap { (k, vList) -> vList.map { k to it } })
        }
        request.body?.let {
            val buffer = okio.Buffer()
            it.writeTo(buffer)
            logBody(buffer.readUtf8(), it.contentType()?.toString(), "Request")
        }
        Log_OC.d(TAG, "-------------------------")
    }

    fun interceptOkHttp3Response(response: Response) {
        Log_OC.d(TAG, "⬅️ Status: ${response.code}")
        response.headers?.toMultimap()?.let { headerMap ->
            logHeaders(headerMap.flatMap { (k, vList) -> vList.map { k to it } })
        }

        val body =
            try {
                response.peekBody(Long.MAX_VALUE)
            } catch (_: Exception) {
                null
            }

        body?.string()?.let { bodyString ->
            logBody(bodyString, response.body?.contentType()?.toString(), "Response")
        }

        Log_OC.d(TAG, "-------------------------")
    }

    fun interceptOkHttpMethodBaseRequest(method: OkHttpMethodBase) {
        Log_OC.d(TAG, "➡️ Method: ${method.javaClass.simpleName} 🌐 URL: ${method.uri}")
        logHeaders(method.requestHeaders.map { it.key to it.value })
        logBody(method.getRequestBodyAsString(), method.getRequestHeader("Content-Type"), "Request")
        Log_OC.d(TAG, "-------------------------")
    }

    fun interceptOkHttpMethodBaseResponse(
        method: OkHttpMethodBase,
        statusCode: Int
    ) {
        Log_OC.d(TAG, "⬅️ Status Code: $statusCode")
        logHeaders(method.getResponseHeaders().toMultimap().flatMap { (k, vList) -> vList.map { k to it } })
        logBody(method.getResponseBodyAsString(), method.getResponseHeader("Content-Type"), "Response")
        Log_OC.d(TAG, "-------------------------")
    }

    // region Private Methods
    private val xmlDocBuilder =
        DocumentBuilderFactory
            .newInstance()
            .apply {
                isNamespaceAware = true
                isIgnoringComments = true
                isIgnoringElementContentWhitespace = true
            }.newDocumentBuilder()

    private val threadLocalTransformer = ThreadLocal<Transformer>()

    private fun getTransformer(): Transformer {
        var transformer = threadLocalTransformer.get()
        if (transformer == null) {
            transformer =
                TransformerFactory.newInstance().newTransformer().apply {
                    setOutputProperty(OutputKeys.INDENT, "yes")
                    setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
                    setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
                    setOutputProperty(OutputKeys.ENCODING, "UTF-8")
                }
            threadLocalTransformer.set(transformer)
        }
        return transformer
    }

    private fun formatXml(xml: String): String =
        try {
            val characterStream = StringReader(xml)
            val inputSource = InputSource(characterStream)
            val doc = xmlDocBuilder.parse(inputSource)
            val writer = StringWriter()
            val domSource = DOMSource(doc)
            val streamResult = StreamResult(writer)
            getTransformer().transform(domSource, streamResult)
            writer.toString()
        } catch (_: Exception) {
            xml
        }

    private fun formatJson(
        json: String,
        indent: Int = 2
    ): String =
        try {
            val trimmed = json.trim()
            when {
                ResponseFormatDetector.isJsonObject(trimmed) -> JSONObject(trimmed).toString(indent)
                ResponseFormatDetector.isJsonArray(trimmed) -> JSONArray(trimmed).toString(indent)
                else -> json
            }
        } catch (_: Exception) {
            json
        }

    private fun formatBody(
        body: String,
        contentType: String
    ): String {
        val bodyFormat = ResponseFormatDetector.detectFormat(body)

        return when {
            contentType.contains("xml", true) || bodyFormat == ResponseFormat.XML -> formatXml(body)
            contentType.contains("json", true) || bodyFormat == ResponseFormat.JSON -> formatJson(body)
            else -> body
        }
    }

    private fun isValidContentType(contentType: String): Boolean =
        contentType.contains("application/json") ||
            contentType.contains("text") ||
            contentType.contains("xml") ||
            contentType.isEmpty()

    private fun logHeaders(headers: Iterable<Pair<String, String>>) {
        headers.forEach { (name, value) -> Log_OC.d(TAG, "📑 Header: $name: $value") }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun logBody(
        body: String?,
        contentType: String?,
        label: String
    ) {
        if (!body.isNullOrBlank() && isValidContentType(contentType ?: "")) {
            try {
                val formatted = formatBody(body, contentType ?: "")
                Log_OC.d(TAG, "📦 $label Body:\n$formatted")
            } catch (e: Exception) {
                Log_OC.w(TAG, "⚠️ Error reading $label body: $e")
            }
        }
    }
    // endregion
}
