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
package com.nextcloud.android.lib.resources.directediting

import com.owncloud.android.AbstractIT
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import org.junit.Test

class DirectEditingObtainRemoteOperationIT : AbstractIT() {
    @Test
    fun testGetAll() {
        val result = DirectEditingObtainRemoteOperation().execute(client)
        assertTrue(result.isSuccess)

        val (editors, creators) = result.resultData
        assertTrue(editors.containsKey("text"))

        val textEditor = editors["text"]
        assertNotNull(textEditor)
        assertEquals("Nextcloud Text", textEditor!!.name)
        assertTrue(textEditor.mimetypes.contains("text/markdown"))
        assertTrue(textEditor.mimetypes.contains("text/plain"))
        assertEquals(0, textEditor.optionalMimetypes.size.toLong())

        val creator = creators["textdocument"]
        assertNotNull(creator)
        assertFalse(creator!!.templates)
    }
}
