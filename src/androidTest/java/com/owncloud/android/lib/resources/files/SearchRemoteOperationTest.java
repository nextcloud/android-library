/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2019 Tobias Kaminsky
 *   Copyright (C) 2019 Nextcloud GmbH
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
package com.owncloud.android.lib.resources.files;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.model.RemoteFile;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class SearchRemoteOperationTest extends AbstractIT {
    @Test
    public void testSearchByFileIdEmpty() {
        SearchRemoteOperation sut = new SearchRemoteOperation("123123",
                                                              SearchRemoteOperation.SearchType.FILE_ID_SEARCH,
                                                              false,
                                                              userId);

        RemoteOperationResult result = sut.execute(client);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getData().size());
    }

    @Test
    public void testSearchByFileIdSuccess() {
        assertTrue(new CreateFolderRemoteOperation("/test/", true).execute(client).isSuccess());

        RemoteOperationResult readFile = new ReadFileRemoteOperation("/test/").execute(client);
        assertTrue(readFile.isSuccess());

        String remoteId = ((RemoteFile) readFile.getData().get(0)).getRemoteId();
        String localId = remoteId.substring(0, 8).replaceAll("^0*", "");

        SearchRemoteOperation sut = new SearchRemoteOperation(localId,
                                                              SearchRemoteOperation.SearchType.FILE_ID_SEARCH,
                                                              false,
                                                              userId);

        RemoteOperationResult result = sut.execute(client);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertEquals("/test/", ((RemoteFile) result.getData().get(0)).getRemotePath());
    }
}
