/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2023 Tobias Kaminsky
 *   Copyright (C) 2023 Nextcloud GmbH
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
