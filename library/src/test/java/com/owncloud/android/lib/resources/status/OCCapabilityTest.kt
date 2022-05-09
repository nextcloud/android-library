/*
 * Nextcloud Android Library is available under MIT license
 *
 * @author Álvaro Brey Vilas
 * Copyright (C) 2022 Álvaro Brey Vilas
 * Copyright (C) 2022 Nextcloud GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.owncloud.android.lib.resources.status

import org.junit.Test

class OCCapabilityTest {

    /**
     * This won't compile if the fields below are not nullable. This is sort of redundant,
     * but it's meant to prevent a crash in client apps when trying to assign null from Java.
     */
    @Test
    fun testFieldNullability() {
        OCCapability().apply {
            accountName = null
            versionString = null
            versionEdition = null
            serverName = null
            serverSlogan = null
            serverColor = null
            serverTextColor = null
            serverElementColor = null
            serverElementColorBright = null
            serverElementColorDark = null
            serverLogo = null
            serverBackground = null
            richDocumentsMimeTypeList = null
            richDocumentsOptionalMimeTypeList = null
            richDocumentsProductName = null
            directEditingEtag = null
        }
    }
}
