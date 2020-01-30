/*
 *
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2020 Tobias Kaminsky
 * Copyright (C) 2020 Nextcloud GmbH
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
    }
}
