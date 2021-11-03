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

package com.owncloud.android.lib.common

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchResultEntryTest {

    @Test
    fun testFilePath() {
        val sut = SearchResultEntry()
        assertEquals(
            "/version/testVersion.txt",
            setPath(
                "http://localhost/nc/index.php/apps/files/?dir=/version&scrollto=testVersion.txt",
                sut
            )
        )
        assertEquals(
            "/_act/test.txt",
            setPath("http:/localhost/nc/index.php/apps/files/?dir=/_act&scrollto=test.txt", sut)
        )
        assertEquals(
            "/_version/test.md",
            setPath("http://localhost/nc/index.php/apps/files/?dir=/_version&scrollto=test.md", sut)
        )
        assertEquals(
            "/allTypes/testWithLink.md",
            setPath(
                "http://localhost/nc/index.php/apps/files/?dir=/allTypes&scrollto=testWithLink.md",
                sut
            )
        )
        assertEquals(
            "/Notes/ShoppingList/Test 123.txt",
            setPath(
                "http://localhost/nc/index.php/apps/files/" +
                    "?dir=/Notes/ShoppingList&scrollto=Test%20123.txt",
                sut
            )
        )
        assertEquals(
            "/[Reportage] Test",
            setPath(
                "http://localhost/nc/index.php/apps/files/?dir=/&scrollto=%5BReportage%5D%20Test",
                sut
            )
        )
        assertEquals(
            "/file.txt",
            setPath("http://localhost/nc/index.php/apps/files/?dir=/&scrollto=file.txt", sut)
        )
    }

    private fun setPath(path: String, entry: SearchResultEntry): String? {
        entry.resourceUrl = path

        return entry.remotePath()
    }
}
