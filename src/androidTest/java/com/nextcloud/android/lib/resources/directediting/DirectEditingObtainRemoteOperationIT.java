/*
 *
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2019 Tobias Kaminsky
 * Copyright (C) 2019 Nextcloud GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.nextcloud.android.lib.resources.directediting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.Creator;
import com.owncloud.android.lib.common.DirectEditing;
import com.owncloud.android.lib.common.Editor;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;

import org.junit.Test;

public class DirectEditingObtainRemoteOperationIT extends AbstractIT {

    @Test
    public void testGetAll() {
        RemoteOperationResult<DirectEditing> result = new DirectEditingObtainRemoteOperation().execute(client);
        assertTrue(result.isSuccess());

        DirectEditing directEditing = result.getResultData();

        assertTrue(directEditing.getEditors().containsKey("text"));

        Editor textEditor = directEditing.getEditors().get("text");
        assertNotNull(textEditor);

        assertEquals("Nextcloud Text", textEditor.getName());

        assertTrue(textEditor.getMimetypes().contains("text/markdown"));
        assertTrue(textEditor.getMimetypes().contains("text/plain"));
        assertEquals(0, textEditor.getOptionalMimetypes().size());

        Creator creator = directEditing.getCreators().get("textdocument");
        assertNotNull(creator);
        assertFalse(creator.templates);
    }
}
