/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.common

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class NextcloudAuthenticator(private val credentials: String) : Authenticator {
    @Suppress("ReturnCount")
    override fun authenticate(
        route: Route?,
        response: Response
    ): Request? {
        val authenticatorType = "Authorization"

        if (response.request.header(authenticatorType) != null) {
            return null
        }

        var countedResponse: Response? = response

        var attemptsCount = 0

        countedResponse = countedResponse?.priorResponse

        while (countedResponse != null) {
            attemptsCount++
            if (attemptsCount == MAX_ATTEMPTS) {
                return null
            }

            countedResponse = countedResponse.priorResponse
        }

        return response.request.newBuilder()
            .header(authenticatorType, credentials)
            .build()
    }

    companion object {
        const val MAX_ATTEMPTS = 3
    }
}
