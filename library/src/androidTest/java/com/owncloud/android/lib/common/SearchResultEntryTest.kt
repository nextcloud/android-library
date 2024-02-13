/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2020 Tobias Kaminsky
 *   Copyright (C) 2020 Nextcloud GmbH
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

    private fun setPath(
        path: String,
        entry: SearchResultEntry
    ): String? {
        entry.resourceUrl = path

        return entry.remotePath()
    }
}
