/*
 * Nextcloud Android Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019-2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2022 Álvaro Brey Vilas <alvaro.brey@nextcloud.com>
 * SPDX-FileCopyrightText: 2020 Daniel Kesselberg <mail@danielkesselberg.de>
 * SPDX-License-Identifier: MIT
 */
package com.owncloud.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.status.CapabilityBooleanType;
import com.owncloud.android.lib.resources.status.E2EVersion;
import com.owncloud.android.lib.resources.status.GetCapabilitiesRemoteOperation;
import com.owncloud.android.lib.resources.status.NextcloudVersion;
import com.owncloud.android.lib.resources.status.OCCapability;
import com.owncloud.android.lib.resources.status.OwnCloudVersion;

import org.junit.Test;

/**
 * Class to test GetRemoteCapabilitiesOperation
 */
public class GetCapabilitiesRemoteOperationIT extends AbstractIT {
    /**
     * Test get capabilities
     */
    @Test
    public void testGetRemoteCapabilitiesOperation() {
        // get capabilities
        RemoteOperationResult result = new GetCapabilitiesRemoteOperation().execute(client);
        assertTrue(result.isSuccess());
        assertTrue(result.getData() != null && result.getData().size() == 1);

        OCCapability capability = (OCCapability) result.getData().get(0);
        checkCapability(capability, client.getUserId());
    }

    @Test
    public void testGetRemoteCapabilitiesOperationEtag() {
        // get capabilities
        RemoteOperationResult result = new GetCapabilitiesRemoteOperation().execute(client);
        assertTrue(result.isSuccess());
        assertTrue(result.getData() != null && result.getData().size() == 1);

        OCCapability capability = (OCCapability) result.getData().get(0);

        RemoteOperationResult resultEtag = new GetCapabilitiesRemoteOperation(capability).execute(client);
        assertTrue(resultEtag.isSuccess());
        assertTrue(resultEtag.getData() != null && resultEtag.getData().size() == 1);

        OCCapability sameCapability = (OCCapability) resultEtag.getData().get(0);

        if (capability.getVersion().isNewerOrEqual(OwnCloudVersion.nextcloud_19)) {
            assertEquals(capability, sameCapability);
        } else {
            assertEquals(capability.getEtag(), sameCapability.getEtag());
        }

        checkCapability(capability, client.getUserId());
    }

    /**
     * Test get capabilities
     */
    @Test
    public void testGetRemoteCapabilitiesOperationWithNextcloudClient() {
        // get capabilities
        RemoteOperationResult result = new GetCapabilitiesRemoteOperation().execute(nextcloudClient);
        assertTrue(result.isSuccess());
        assertTrue(result.getData() != null && result.getData().size() == 1);

        OCCapability capability = (OCCapability) result.getData().get(0);
        checkCapability(capability, client.getUserId());
    }

    @Test
    public void testGetRemoteCapabilitiesOperationEtagWithNextcloudClient() {
        // get capabilities
        RemoteOperationResult result = new GetCapabilitiesRemoteOperation().execute(nextcloudClient);
        assertTrue(result.isSuccess());
        assertTrue(result.getData() != null && result.getData().size() == 1);

        OCCapability capability = (OCCapability) result.getData().get(0);

        RemoteOperationResult resultEtag = new GetCapabilitiesRemoteOperation(capability).execute(nextcloudClient);
        assertTrue(resultEtag.isSuccess());
        assertTrue(resultEtag.getData() != null && resultEtag.getData().size() == 1);

        OCCapability sameCapability = (OCCapability) resultEtag.getData().get(0);

        if (capability.getVersion().isNewerOrEqual(OwnCloudVersion.nextcloud_19)) {
            assertEquals(capability, sameCapability);
        } else {
            assertEquals(capability.getEtag(), sameCapability.getEtag());
        }

        checkCapability(capability, nextcloudClient.getUserId());
    }

    private void checkCapability(OCCapability capability, String userId) {
        assertTrue(capability.getActivity().isTrue());
        assertTrue(capability.getFilesSharingApiEnabled().isTrue());
        assertTrue(capability.getFilesVersioning().isTrue());
        assertTrue(capability.getFilesUndelete().isTrue());
        assertNotNull(capability.getVersion());
        assertFalse(capability.getEtag().isEmpty());
        assertSame(CapabilityBooleanType.FALSE, capability.getRichDocuments());
        assertFalse(capability.getDirectEditingEtag().isEmpty());
        assertSame(CapabilityBooleanType.UNKNOWN, capability.getDropAccount());

        // user status
        if (capability.getVersion().isNewerOrEqual(OwnCloudVersion.nextcloud_20)) {
            assertTrue(capability.getUserStatus().isTrue());
            assertTrue(capability.getUserStatusSupportsEmoji().isTrue());
        } else {
            assertFalse(capability.getUserStatus().isTrue());
            assertFalse(capability.getUserStatusSupportsEmoji().isTrue());
        }

        // locking
        if (capability.getVersion().isNewerOrEqual(NextcloudVersion.nextcloud_24)) {
            // files_lock app needs to be installed in server for this to work
            assertNotNull(capability.getFilesLockingVersion());
        }

        // groupfolder
        if (capability.getVersion().isNewerOrEqual(NextcloudVersion.nextcloud_27)) {
            if (userId.equals("test")) {
                assertTrue(capability.getGroupfolders().isTrue());
            } else {
                assertTrue(capability.getGroupfolders().isFalse());
            }
        } else {
            assertTrue(capability.getGroupfolders().isFalse());
        }

        // assistant
        if (capability.getVersion().isNewerOrEqual(NextcloudVersion.nextcloud_28)) {
            if (userId.equals("test")) {
                assertTrue(capability.getAssistant().isTrue());
            } else {
                assertFalse(capability.getAssistant().isFalse());
            }
        }

        // e2e
        assertNotSame(capability.getEndToEndEncryptionApiVersion(), E2EVersion.UNKNOWN);

        // recommendations
        if (capability.getVersion().isNewerOrEqual(NextcloudVersion.nextcloud_31)) {
            assertTrue(capability.getRecommendations().isTrue());
        }
    }
}
