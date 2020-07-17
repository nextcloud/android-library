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
package com.owncloud.android;

import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.status.CapabilityBooleanType;
import com.owncloud.android.lib.resources.status.GetCapabilitiesRemoteOperation;
import com.owncloud.android.lib.resources.status.OCCapability;
import com.owncloud.android.lib.resources.status.OwnCloudVersion;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Class to test GetRemoteCapabilitiesOperation
 */
public class GetCapabilitiesTest extends AbstractIT {
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
        checkCapability(capability);
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

        checkCapability(capability);
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
        checkCapability(capability);
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

        checkCapability(capability);
    }

    private void checkCapability(OCCapability capability) {
        assertTrue(capability.getActivity().isTrue());
        assertTrue(capability.getFilesSharingApiEnabled().isTrue());
        assertTrue(capability.getFilesVersioning().isTrue());
        assertTrue(capability.getFilesUndelete().isTrue());
        assertNotNull(capability.getVersion());
        assertFalse(capability.getEtag().isEmpty());
        assertSame(capability.getRichDocuments(), CapabilityBooleanType.FALSE);
        assertFalse(capability.getDirectEditingEtag().isEmpty());
    }
}
