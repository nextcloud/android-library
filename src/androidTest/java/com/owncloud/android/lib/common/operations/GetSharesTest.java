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
package com.owncloud.android.lib.common.operations;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation;
import com.owncloud.android.lib.resources.shares.CreateShareRemoteOperation;
import com.owncloud.android.lib.resources.shares.GetSharesRemoteOperation;
import com.owncloud.android.lib.resources.shares.OCShare;
import com.owncloud.android.lib.resources.shares.ShareType;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Class to test Get Shares Operation
 *
 * @author masensio
 */

public class GetSharesTest extends AbstractIT {
    @Test    
    public void testGetShares() {
        assertTrue(new CreateFolderRemoteOperation("/1/", true).execute(client).isSuccess());
        assertTrue(new CreateShareRemoteOperation("/1/",
                                                  ShareType.PUBLIC_LINK,
                                                  "",
                                                  false,
                                                  "",
                                                  1).execute(client).isSuccess());

        assertTrue(new CreateFolderRemoteOperation("/2/", true).execute(client).isSuccess());
        assertTrue(new CreateShareRemoteOperation("/2/",
                                                  ShareType.PUBLIC_LINK,
                                                  "",
                                                  false,
                                                  "",
                                                  1).execute(client).isSuccess());

        RemoteOperationResult result = new GetSharesRemoteOperation().execute(client);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
        assertEquals("/1/", ((OCShare) result.getData().get(0)).getPath());
        assertEquals("/2/", ((OCShare) result.getData().get(1)).getPath());
    }
}
