/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2018 Tobias Kaminsky
 *   Copyright (C) 2018 Nextcloud GmbH
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

package com.owncloud.android.utils;

import com.owncloud.android.lib.resources.status.OwnCloudVersion;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OwncloudVersionTest {
    @Test
    public void testOwnCloudVersion() {
        OwnCloudVersion version = new OwnCloudVersion("12.0.0");

        assertTrue(version.isNewerOrEqual(OwnCloudVersion.nextcloud_12));
    }

    @Test
    public void testOwnCloudVersionFailure() {
        OwnCloudVersion version = new OwnCloudVersion("");

        assertFalse(version.isVersionValid());
    }

    @Test
    public void testSupportNC12() {
        assertTrue(OwnCloudVersion.nextcloud_12.isSearchSupported());
        assertTrue(OwnCloudVersion.nextcloud_12.isWebLoginSupported());
        assertFalse(OwnCloudVersion.nextcloud_12.isMediaStreamingSupported());
        assertFalse(OwnCloudVersion.nextcloud_12.isNoteOnShareSupported());
        assertFalse(OwnCloudVersion.nextcloud_12.isHideFileDownloadSupported());
        assertFalse(OwnCloudVersion.nextcloud_12.isShareesOnDavSupported());
    }

    @Test
    public void testSupportNC13() {
        assertTrue(OwnCloudVersion.nextcloud_13.isSearchSupported());
        assertTrue(OwnCloudVersion.nextcloud_13.isWebLoginSupported());
        assertFalse(OwnCloudVersion.nextcloud_13.isMediaStreamingSupported());
        assertFalse(OwnCloudVersion.nextcloud_13.isNoteOnShareSupported());
        assertFalse(OwnCloudVersion.nextcloud_12.isHideFileDownloadSupported());
        assertFalse(OwnCloudVersion.nextcloud_12.isShareesOnDavSupported());
    }

    @Test
    public void testSupportNC14() {
        assertTrue(OwnCloudVersion.nextcloud_14.isSearchSupported());
        assertTrue(OwnCloudVersion.nextcloud_14.isWebLoginSupported());
        assertTrue(OwnCloudVersion.nextcloud_14.isMediaStreamingSupported());
        assertTrue(OwnCloudVersion.nextcloud_14.isNoteOnShareSupported());
        assertFalse(OwnCloudVersion.nextcloud_14.isHideFileDownloadSupported());
        assertFalse(OwnCloudVersion.nextcloud_14.isShareesOnDavSupported());
    }

    @Test
    public void testSupportNC15() {
        assertTrue(OwnCloudVersion.nextcloud_15.isSearchSupported());
        assertTrue(OwnCloudVersion.nextcloud_15.isWebLoginSupported());
        assertTrue(OwnCloudVersion.nextcloud_15.isMediaStreamingSupported());
        assertTrue(OwnCloudVersion.nextcloud_15.isNoteOnShareSupported());
        assertTrue(OwnCloudVersion.nextcloud_15.isHideFileDownloadSupported());
        assertFalse(OwnCloudVersion.nextcloud_15.isShareesOnDavSupported());
    }

    @Test
    public void testSupportNC16() {
        assertTrue(OwnCloudVersion.nextcloud_16.isSearchSupported());
        assertTrue(OwnCloudVersion.nextcloud_16.isWebLoginSupported());
        assertTrue(OwnCloudVersion.nextcloud_16.isMediaStreamingSupported());
        assertTrue(OwnCloudVersion.nextcloud_16.isNoteOnShareSupported());
        assertTrue(OwnCloudVersion.nextcloud_16.isHideFileDownloadSupported());
        assertFalse(OwnCloudVersion.nextcloud_16.isShareesOnDavSupported());
    }

    @Test
    public void testSupportNC17() {
        assertTrue(OwnCloudVersion.nextcloud_17.isSearchSupported());
        assertTrue(OwnCloudVersion.nextcloud_17.isWebLoginSupported());
        assertTrue(OwnCloudVersion.nextcloud_17.isMediaStreamingSupported());
        assertTrue(OwnCloudVersion.nextcloud_17.isNoteOnShareSupported());
        assertTrue(OwnCloudVersion.nextcloud_17.isHideFileDownloadSupported());
        assertTrue(OwnCloudVersion.nextcloud_17.isShareesOnDavSupported());
    }
}
