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

package com.nextcloud.android.lib.resources.users;

import android.text.TextUtils;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.OwnCloudBasicCredentials;
import com.owncloud.android.lib.common.OwnCloudCredentials;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation;

import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotEquals;

public class GenerateAppPasswordRemoteOperationTest extends AbstractIT {

    @Test
    public void generateAppPassword() {
        GenerateAppPasswordRemoteOperation sut = new GenerateAppPasswordRemoteOperation();
        RemoteOperationResult result = sut.execute(client);

        assertTrue(result.isSuccess());

        String appPassword = (String) result.getSingleData();
        assertFalse(TextUtils.isEmpty(appPassword));

        OwnCloudCredentials oldOwnCloudCredentials = client.getCredentials();
        OwnCloudCredentials newOwnCloudCredentials = new OwnCloudBasicCredentials(oldOwnCloudCredentials.getUsername(),
                appPassword);

        assertNotEquals(oldOwnCloudCredentials, newOwnCloudCredentials);

        client.setCredentials(newOwnCloudCredentials);

        assertTrue(new ReadFolderRemoteOperation("/").execute(client).isSuccess());

        // using app password to generate new password should fail
        assertFalse(new GenerateAppPasswordRemoteOperation().execute(client).isSuccess());
    }
}
