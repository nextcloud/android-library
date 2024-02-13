/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2022 Tobias Kaminsky
 *   Copyright (C) 2022 Nextcloud GmbH
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

package com.nextcloud.android.lib.resources.dashboard

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.status.NextcloudVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardListWidgetsRemoteOperationIT : AbstractIT() {
    @Test
    fun list() {
        // only on NC25+
        testOnlyOnServer(NextcloudVersion.nextcloud_25)

        val result = DashboardListWidgetsRemoteOperation().execute(nextcloudClient)
        assertTrue(result.isSuccess)

        assertTrue(result.resultData.isNotEmpty())

        assertTrue(result.resultData["recommendations"]?.buttons?.getOrNull(0) == null)

        assertEquals(1, result.resultData["activity"]?.buttons?.size)
        assertTrue(result.resultData["activity"]?.buttons?.getOrNull(0)?.type == DashBoardButtonType.MORE)
        assertTrue(result.resultData["activity"]?.roundIcons == false)

        assertTrue(result.resultData["user_status"]?.roundIcons == true)
    }
}
