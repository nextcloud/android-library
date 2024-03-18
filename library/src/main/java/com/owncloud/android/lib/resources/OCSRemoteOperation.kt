/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019-2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2018 Bartosz Przybylski <bart.p.pl@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.nextcloud.common.OkHttpMethodBase
import com.owncloud.android.lib.common.operations.RemoteOperation
import org.apache.commons.httpclient.HttpMethodBase
import java.io.IOException

/**
 * Base class for OCS remote operations with convenient methods
 *
 * @author Bartosz Przybylski
 */
abstract class OCSRemoteOperation<T> : RemoteOperation<T>() {
    @Deprecated("Use OkHttpMethodBase variant instead")
    fun <T> getServerResponse(
        method: HttpMethodBase,
        type: TypeToken<T>
    ): T? {
        return try {
            val response = method.responseBodyAsString
            val element: JsonElement = JsonParser.parseString(response)
            gson.fromJson(element, type.type)
        } catch (ioException: IOException) {
            null
        } catch (syntaxException: JsonSyntaxException) {
            null
        }
    }

    fun <T> getServerResponse(
        method: OkHttpMethodBase,
        type: TypeToken<T>
    ): T? {
        return try {
            val response = method.getResponseBodyAsString()
            val element: JsonElement = JsonParser.parseString(response)
            gson.fromJson(element, type.type)
        } catch (syntaxException: JsonSyntaxException) {
            null
        }
    }
}
