/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Álvaro Brey Vilas <alvaro.brey@nextcloud.com>
 * SPDX-FileCopyrightText: 2020 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
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
