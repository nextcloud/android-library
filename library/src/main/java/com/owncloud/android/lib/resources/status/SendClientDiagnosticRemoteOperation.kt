/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android.lib.resources.status

import androidx.annotation.VisibleForTesting
import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.PutMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.apache.commons.httpclient.HttpStatus

class SendClientDiagnosticRemoteOperation(
    private val syncConflict: Problem?,
    private val problems: List<Problem>?,
    private val virusDetected: Problem?,
    private val e2eError: Problem?
) : RemoteOperation<Void>() {
    override fun run(client: NextcloudClient): RemoteOperationResult<Void> {
        val request = RequestBody.create("application/json".toMediaTypeOrNull(), generateJSON())

        val putMethod = PutMethod(client.baseUri.toString() + URL, true, request)

        val status = putMethod.execute(client)

        return if (status == HttpStatus.SC_OK) {
            RemoteOperationResult<Void>(true, putMethod)
        } else {
            RemoteOperationResult<Void>(false, putMethod)
        }
    }

    @VisibleForTesting
    fun generateJSON(): String {
        val map = mutableListOf<String>()

        if (syncConflict != null) {
            map.add(syncConflict.toJsonWithTypeString())
        } else {
            map.add(""""$SYNC_CONFLICTS": {}""")
        }

        if (problems != null) {
            val test = problems.map { it.toJsonWithTypeString() }
            map.add(""""$PROBLEMS": ${test.joinToString(", ", "{", "}")}""")
        } else {
            map.add(""""$PROBLEMS": {}""")
        }

        if (virusDetected != null) {
            map.add(virusDetected.toJsonWithTypeString())
        } else {
            map.add(""""$VIRUS_DETECTED": {}""")
        }

        if (e2eError != null) {
            map.add(e2eError.toJsonWithTypeString())
        } else {
            map.add(""""$E2EE_ERRORS": {}""")
        }

        return map.joinToString(prefix = "{", postfix = "}")
    }

    companion object {
        const val URL = "/ocs/v2.php/apps/security_guard/diagnostics"

        const val SYNC_CONFLICTS = "sync_conflicts"
        const val PROBLEMS = "problems"
        const val VIRUS_DETECTED = "virus_detected"
        const val E2EE_ERRORS = "e2ee_errors"
    }
}
