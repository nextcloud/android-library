/*
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2020 Tobias Kaminsky
 * Copyright (C) 2020 Nextcloud GmbH
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.owncloud.android

import com.nextcloud.common.PlainClient
import com.nextcloud.operations.GetMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.net.HttpURLConnection

class PlainClientIT : AbstractIT() {
    @Test
    fun test204Success() {
        val sut = PlainClient(context)
        val getMethod = GetMethod("$url/index.php/204", false)

        val status = getMethod.execute(sut)

        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, status)
    }

    @Test
    fun test204Error() {
        val sut = PlainClient(context)
        val getMethod = GetMethod("$url/index.php", false)

        val status = getMethod.execute(sut)

        assertNotEquals(HttpURLConnection.HTTP_NO_CONTENT, status)
    }
}
