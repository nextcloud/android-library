/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2022-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2022 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: MIT
 */
package com.nextcloud.android.lib.resources.directediting

import com.owncloud.android.AbstractIT
import com.owncloud.android.lib.resources.status.OwnCloudVersion
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DirectEditingObtainRemoteOperationIT : AbstractIT() {
    @Before
    fun before() {
        testOnlyOnServer(OwnCloudVersion.nextcloud_18)
    }

    @Test
    fun testGetAll() {
        val result = DirectEditingObtainRemoteOperation().run(nextcloudClient)
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
